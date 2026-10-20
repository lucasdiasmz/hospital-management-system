# Hospital Vida — API (Sprint 2)

API REST em **Spring Boot 3.5 + JPA + H2** com as classes de domínio, regras de negócio e endpoints da Sprint 2.
O diagrama de classes atualizado está em [`../docs/diagrama-classes-sprint2.pdf`](../docs/diagrama-classes-sprint2.pdf).

## Como rodar

Requisitos: **Java 17+** e Maven (ou a IDE: IntelliJ / VS Code com Extension Pack for Java).

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`, já com dados de exemplo (3 pacientes, 4 profissionais, 4 quartos, consultas,
uma emergência, um procedimento e uma internação), criados pelo `DataLoader`.
O banco é em memória: ao reiniciar, os dados voltam ao estado inicial.

- Console do banco: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:hospitalvida`, usuário `sa`, sem senha)
- Testes: `mvn test`

## Organização

```
model/          classes de domínio + regras de negócio
  enums/        situações, prioridades, tipos (nada de texto livre)
repository/     acesso ao banco (Spring Data JPA)
service/        regras que dependem do banco (CPF duplicado, conflito de agenda...)
controller/     endpoints REST
dto/            formatos do JSON de entrada
exception/      exceções + tradução para respostas HTTP
config/         dados de exemplo e CORS
```

## Como os requisitos foram atendidos

| Requisito | Onde está |
|---|---|
| Categorias de profissionais | `ProfissionalSaude` (abstrata) → `ClinicoGeral`, `MedicoEspecialista`, `Cirurgiao`. Cada uma sobrescreve `realizaConsultaRotina()`, `atendeEspecialidade()` e `podeRealizarCirurgias()` (polimorfismo). |
| Clínico encaminha para especialista | `ClinicoGeral.encaminhar(...)` cria uma `ConsultaEspecializada` com `encaminhadoPor`. |
| Profissionais por área de atuação | `GET /profissionais?especialidade=CARDIOLOGIA` (+ `&disponivelEm=...` para ver só os livres). |
| Procedimentos do cirurgião | `GET /profissionais/{id}/procedimentos` |
| Modalidades sem duplicar atributos | `Atendimento` (abstrata) concentra paciente, profissional, data/hora e observações; `Consulta` (abstrata) concentra motivo e situação. |
| Validação própria de cada modalidade | `Atendimento.validar()` é um *template method*: faz as checagens comuns e chama `validarCondicoes()`, que cada modalidade implementa. |
| Prioridade da emergência | enum `Prioridade` (BAIXA, MODERADA, ALTA, CRITICA). `GET /emergencias?status=AGUARDANDO_AVALIACAO` devolve a fila ordenada por prioridade. |
| Procedimento não conclui sem iniciar | `StatusProcedimento.podeMudarPara()` só permite CONCLUIDO a partir de EM_ANDAMENTO. |
| Situação da consulta consistente | enum `SituacaoConsulta` com as transições permitidas. Concluída não volta a agendada; cancelada só é atendida via **reagendamento** (`POST /consultas/{id}/reagendamento`), que cria uma nova consulta ligada à original. |
| Tipos de quarto | enum `TipoQuarto`: limite de leitos por tipo (Enfermaria 6, Apartamento 2, UTI 10, Isolamento 1); UTI e Isolamento exigem justificativa clínica na internação. |

### Fluxo das situações da consulta

```
AGENDADA ──► CONFIRMADA ──► EM_ATENDIMENTO ──► CONCLUIDA
   │              │
   ├──► CANCELADA ◄┤            (CONCLUIDA, CANCELADA e NAO_COMPARECEU são finais)
   └──► NAO_COMPARECEU ◄┘
```

### Outras regras implementadas

- CPF (11 dígitos) e registro profissional únicos; e-mail com formato válido.
- Consultas: data futura, de segunda a sábado, das 7h às 19h; rotina dura 30 min e especializada 45 min.
- Ninguém fica com dois atendimentos no mesmo horário (nem o médico, nem o paciente).
- Só o profissional responsável registra observações, e só depois que o atendimento começou.
- Cirurgia eletiva precisa ser agendada para o futuro.
- Paciente não pode ter duas internações ativas; alta e transferência atualizam a ocupação dos quartos.
- Quarto só é bloqueado (manutenção) se estiver vazio; lotado ou bloqueado não recebe internação.
- Paciente, profissional e quarto com registros vinculados não podem ser excluídos.

## Endpoints

Os 5 pedidos no enunciado, mais `/emergencias` e `/procedimentos` para as modalidades que não são consultas.

| Método | Rota | O que faz |
|---|---|---|
| GET | `/pacientes?nome=` | lista (filtro por nome opcional) |
| GET / POST | `/pacientes/{id}` / `/pacientes` | busca / cadastra |
| PUT / DELETE | `/pacientes/{id}` | atualiza / exclui |
| GET / PUT | `/pacientes/{id}/historico` | histórico completo / atualiza informações relevantes |
| GET | `/profissionais?categoria=&especialidade=&disponivelEm=` | lista com filtros |
| GET / POST | `/profissionais/{id}` / `/profissionais` | busca / cadastra (`categoria` define a subclasse) |
| PUT / DELETE | `/profissionais/{id}` | atualiza / exclui |
| GET | `/profissionais/{id}/procedimentos` | procedimentos do cirurgião |
| GET | `/consultas?situacao=&pacienteId=&profissionalId=` | lista |
| GET / POST | `/consultas/{id}` / `/consultas` | busca / agenda (`tipo`: ROTINA ou ESPECIALIZADA) |
| PATCH | `/consultas/{id}/situacao` | muda a situação |
| PATCH | `/consultas/{id}/observacoes` | registra observação do médico responsável |
| POST | `/consultas/{id}/reagendamento` | nova consulta a partir de uma cancelada / falta |
| GET / POST | `/emergencias?status=` / `/emergencias` | fila / registra chegada |
| PATCH | `/emergencias/{id}/iniciar` · `/concluir` | inicia avaliação / conclui |
| GET / POST | `/procedimentos?status=&cirurgiaoId=&pacienteId=` / `/procedimentos` | lista / agenda |
| PATCH | `/procedimentos/{id}/iniciar` · `/concluir` · `/cancelar` | muda o status |
| GET | `/internacoes?status=&pacienteId=` | lista |
| GET / POST | `/internacoes/{id}` / `/internacoes` | busca / interna |
| PATCH | `/internacoes/{id}/alta` · `/transferencia` · `/cancelar` | alta / troca de quarto / cancela |
| GET | `/quartos?situacao=&tipo=&comVaga=` | lista |
| GET / POST / DELETE | `/quartos/{id}` / `/quartos` | busca / cadastra / exclui |
| PATCH | `/quartos/{id}/bloquear` · `/desbloquear` | manutenção |

### Exemplos de corpo (JSON)

```json
POST /profissionais
{ "categoria": "ESPECIALISTA", "nome": "Dra. Júlia", "registroProfissional": "CRM-MG 4040",
  "telefone": "(31) 3333-4040", "email": "julia@hospitalvida.com", "especialidade": "NEUROLOGIA" }

POST /consultas
{ "tipo": "ESPECIALIZADA", "pacienteId": 2, "profissionalId": 2, "dataHora": "2026-11-09T14:00:00",
  "motivo": "Palpitações", "especialidade": "CARDIOLOGIA", "encaminhadoPorId": 1 }

PATCH /consultas/1/situacao
{ "situacao": "CONFIRMADA" }

POST /emergencias
{ "pacienteId": 1, "prioridade": "CRITICA", "queixaPrincipal": "Dor no peito" }

POST /procedimentos
{ "pacienteId": 2, "cirurgiaoId": 4, "descricao": "Hernioplastia", "tipoCirurgia": "ELETIVA",
  "dataHoraPrevista": "2026-11-10T08:00:00", "duracaoPrevistaMinutos": 120,
  "observacoesPreOperatorias": "Jejum de 8h" }

POST /internacoes
{ "pacienteId": 2, "profissionalId": 1, "quartoId": 3, "dataPrevistaAlta": "2026-11-15",
  "observacoes": "Pós-operatório com monitoramento contínuo" }
```

### Respostas de erro

| Status | Quando |
|---|---|
| 400 | JSON inválido ou valor que não existe no enum (a resposta lista os valores aceitos) |
| 404 | id não encontrado |
| 409 | violação de integridade no banco |
| 422 | regra de negócio violada (ex.: `"Não é possível mudar uma consulta de CANCELADA para CONCLUIDA."`) |

```json
{ "status": 422, "erro": "O quarto 201 está lotado.", "timestamp": "2026-10-19T12:00:00" }
```

> Ids dos dados de exemplo: profissionais 1 = Dr. Carlos (clínico), 2 = Dra. Beatriz (cardiologia), 3 = Dr. Diego (ortopedia),
> 4 = Dr. Rafael (cirurgião); quartos 1 = 101 (enfermaria), 2 = 201 (apartamento), 3 = UTI-01, 4 = ISO-01.
