# Hospital Vida - API (Sprint 2)

API REST do sistema hospitalar em Spring Boot, pacote `br.pucminas.hospitalvida`.

## Como executar

Requisitos: Java 21 ou superior e Maven.

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco é H2 em memória (os dados somem ao parar a aplicação);
o console fica em `http://localhost:8080/h2-console` com JDBC URL `jdbc:h2:mem:hospitalvida`, usuário `sa` e senha vazia.

Testes: `mvn test`.

## Estrutura

```text
src/main/java/br/pucminas/hospitalvida/
├── model/          entidades JPA e regras de domínio (enums em model/enums)
├── dto/            corpos de requisição
├── repository/     interfaces Spring Data JPA
├── service/        regras de aplicação, chamadas pelos controllers
├── controller/     endpoints REST
├── config/         liberação de CORS para as telas
└── exception/      exceções de negócio e tratamento global de erros
docs/diagrama-classes.md   diagrama de classes (Mermaid)
```

## Divisão das tarefas

| Pessoa | Parte | Situação |
|---|---|---|
| 1 | Base do projeto, tratamento de erros, pacientes, diagrama, integração | pronto |
| 2 | Profissionais de saúde e encaminhamentos (`docs/pessoa2-profissionais.md`) | integrado |
| 3 | Consultas, emergências e situações (`docs/pessoa3-consultas.md`) | integrado |
| 4 | Procedimento cirúrgico, internações e quartos | integrado |

Uma branch por pessoa, com merge na `main` após revisão de outro integrante.

## O que a base oferece para as outras partes

- `exception/RegraNegocioException(String mensagem)` → HTTP **422**.
- `exception/RecursoNaoEncontradoException(String recurso, Long id)` → HTTP **404**
  (ex.: `new RecursoNaoEncontradoException("Quarto", id)`).
- `model/Paciente` (entidade JPA, `id` `Long`) e `repository/PacienteRepository extends JpaRepository<Paciente, Long>`.
- `service/PacienteService.buscarPorId(id)` devolve o paciente ou lança 404.

## Respostas de erro

Todas no formato `{ "status": 422, "mensagem": "...", "momento": "2026-11-08T20:14:39" }`.

| Situação | HTTP |
|---|---|
| Campo inválido em `@Valid` (lista os campos na mensagem) | 400 |
| JSON malformado ou valor em formato errado (data deve ser `aaaa-mm-dd`, enum inexistente) | 400 |
| Registro não encontrado | 404 |
| Exclusão de registro vinculado a outros (ex.: paciente com encaminhamento) ou valor único duplicado no banco | 409 |
| Regra de negócio violada | 422 |

## Pacientes

| Método | Rota | Descrição |
|---|---|---|
| GET | `/pacientes` | Lista pacientes |
| GET | `/pacientes/{id}` | Busca paciente |
| POST | `/pacientes` | Cadastra paciente (201) |
| PUT | `/pacientes/{id}` | Atualiza paciente |
| DELETE | `/pacientes/{id}` | Remove paciente (204) |

```json
POST /pacientes
{ "nome": "Ana Souza", "cpf": "529.982.247-25", "dataNascimento": "1990-05-10",
  "telefone": "(31) 99999-0000", "email": "ana@hospitalvida.com", "endereco": "Rua A, 100" }
```

Regras:
- Todos os campos são obrigatórios; e-mail com formato válido; data de nascimento no passado.
- CPF aceito com ou sem máscara, com dígitos verificadores conferidos; é guardado só com números e não pode repetir (422).
- Paciente com consultas, internações ou encaminhamentos não é apagado, para preservar o histórico (409).

## Profissionais (Pessoa 2)

Detalhes e exemplos em [`docs/pessoa2-profissionais.md`](../docs/pessoa2-profissionais.md).

| Método | Rota | Descrição |
|---|---|---|
| GET | `/profissionais?categoria=&especialidade=&incluirInativos=` | Lista (por padrão, só ativos) |
| GET | `/profissionais/{id}` | Busca |
| POST | `/profissionais` | Cadastra (`categoria` define a subclasse) |
| PUT | `/profissionais/{id}` | Atualiza nome, contato e atuação cirúrgica |
| PATCH | `/profissionais/{id}/desativar` · `/reativar` | Tira ou volta para as buscas |
| POST | `/profissionais/{id}/encaminhamentos` | Clínico `{id}` encaminha um paciente |
| GET | `/profissionais/{id}/encaminhamentos` | Encaminhamentos feitos pelo clínico |
| GET | `/profissionais/{id}/procedimentos` | Procedimentos do cirurgião (data, paciente e descrição) |

## Consultas e emergências (Pessoa 3)

Detalhes e exemplos em [`docs/pessoa3-consultas.md`](../docs/pessoa3-consultas.md).

| Método | Rota | Descrição |
|---|---|---|
| GET | `/consultas?situacao=&pacienteId=&profissionalId=` | Lista consultas |
| GET | `/consultas/{id}` | Busca |
| POST | `/consultas` | Agenda (`tipo` define a subclasse) |
| PATCH | `/consultas/{id}/situacao` | Troca a situação |
| PATCH | `/consultas/{id}/observacoes` | Registra observações |
| POST | `/consultas/{id}/reagendamento` | Novo agendamento de consulta cancelada ou com falta |
| GET | `/consultas/emergencias?status=` | Fila de emergência, por prioridade |
| GET | `/consultas/emergencias/{id}` | Busca |
| POST | `/consultas/emergencias` | Registra a chegada do paciente |
| PATCH | `/consultas/emergencias/{id}/iniciar` · `/concluir` | Médico assume o caso · conclui |

## Procedimentos cirúrgicos (Pessoa 4)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/consultas/procedimentos?status=&cirurgiaoId=&pacienteId=` | Lista |
| GET | `/consultas/procedimentos/{id}` | Busca |
| POST | `/consultas/procedimentos` | Agenda |
| PATCH | `/consultas/procedimentos/{id}/iniciar` · `/concluir` · `/cancelar` | Muda o status |

```json
POST /consultas/procedimentos
{ "data": "2026-12-01", "horario": "08:00", "pacienteId": 1, "cirurgiaoId": 3,
  "descricao": "Apendicectomia", "tipoCirurgia": "ELETIVA", "observacoesPreOperatorias": "Jejum de 8 horas" }
```

Regras:
- `data` e `horario` são os previstos; `inicioEfetivo` e `fimEfetivo` são preenchidos ao iniciar e concluir.
- `AGENDADO → EM_ANDAMENTO → CONCLUIDO`, ou `AGENDADO → CANCELADO`. Não conclui sem iniciar.
- O responsável precisa ser um cirurgião ativo. Cirurgias eletivas só podem ser marcadas para o futuro.

## Quartos e internações (Pessoa 4)

| Método | Rota | Descrição |
|---|---|---|
| GET | `/quartos?situacao=&tipo=&comVaga=` | Lista |
| GET | `/quartos/{id}` | Busca |
| POST | `/quartos` | Cadastra |
| PATCH | `/quartos/{id}/bloquear` · `/desbloquear` | Tira ou volta para uso |
| DELETE | `/quartos/{id}` | Remove (só sem internações registradas) |
| GET | `/internacoes?status=&pacienteId=` | Lista |
| GET | `/internacoes/{id}` | Busca |
| POST | `/internacoes` | Interna e ocupa um leito |
| PATCH | `/internacoes/{id}/alta` | Registra a alta e libera o leito |
| PATCH | `/internacoes/{id}/transferencia` | Muda de quarto |
| PATCH | `/internacoes/{id}/cancelar` | Cancela e libera o leito |

```json
POST /quartos
{ "numero": "101", "andar": 1, "tipo": "ENFERMARIA", "capacidadeMaxima": 4 }

POST /internacoes
{ "pacienteId": 1, "profissionalId": 1, "quartoId": 1, "dataPrevistaAlta": "2026-12-05", "observacoes": "Pneumonia leve" }
```

| Tipo de quarto | Limite de leitos | Justificativa clínica nas observações |
|---|---|---|
| `ENFERMARIA` | 6 | não |
| `APARTAMENTO` | 2 | não |
| `UTI` | 10 | sim |
| `ISOLAMENTO` | 1 | sim |

Regras:
- A situação do quarto acompanha a ocupação; só quarto vazio pode ser bloqueado.
- O paciente só pode ter uma internação ativa, e quartos lotados ou bloqueados não recebem pacientes.
- Na transferência, o leito novo é ocupado antes de liberar o antigo.
