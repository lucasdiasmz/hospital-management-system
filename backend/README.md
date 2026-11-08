# Hospital Vida - API (Sprint 2)

API REST do sistema hospitalar em Spring Boot. Este é o **esqueleto** da Sprint 2: a estrutura,
as entidades, os endpoints e o tratamento de erros já existem; as regras de negócio estão
marcadas com `TODO (Pessoa N)` para cada integrante preencher.

## Como executar

Requisitos: Java 21 ou superior e Maven.

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. O banco é H2 em memória (os dados somem ao parar a aplicação);
o console fica em `http://localhost:8080/h2-console` com JDBC URL `jdbc:h2:mem:hospital`, usuário `sa` e senha vazia.

Para rodar os testes: `mvn test`.

Enquanto um método ainda tiver `TODO`, o endpoint correspondente responde **501 Not Implemented**
com o nome do método que falta.

## Estrutura

```text
src/main/java/br/pucminas/hospital/
├── model/          entidades JPA e regras de domínio
│   └── enums/      situações, prioridades, especialidades, tipos
├── dto/            corpos de requisição (records)
├── repository/     interfaces Spring Data JPA
├── service/        regras de aplicação, chamadas pelos controllers
├── controller/     endpoints REST
└── exception/      exceções de negócio e tratamento global (404, 422, 400, 501)
docs/diagrama-classes.md   diagrama de classes (Mermaid)
```

## Divisão das tarefas

| Pessoa | Parte | Arquivos |
|---|---|---|
| 1 | Pacientes, base do projeto, diagrama, integração | `Paciente`, `PacienteRepository`, `PacienteService`, `PacienteController`, `GlobalExceptionHandler`, `docs/diagrama-classes.md` |
| 2 | Profissionais de saúde | `Profissional`, `MedicoClinicoGeral`, `MedicoEspecialista`, `Cirurgiao`, `Especialidade`, `TipoProfissional`, `ProfissionalRequest`, `ProfissionalRepository`, `ProfissionalService`, `ProfissionalController` |
| 3 | Consultas e situações | `Consulta`, `ConsultaRotina`, `ConsultaEspecializada`, `AtendimentoEmergencia`, `SituacaoConsulta`, `Prioridade`, `StatusEmergencia`, `AtendimentoRequest`, `AtendimentoRepository`, `AtendimentoService`, `ConsultaController` |
| 4 | Procedimento cirúrgico, internações e quartos | `ProcedimentoCirurgico`, `StatusProcedimento`, `Quarto`, `TipoQuarto`, `Internacao`, `InternacaoRequest`, `QuartoRepository`, `QuartoService`, `QuartoController`, `InternacaoRepository`, `InternacaoService`, `InternacaoController` |

Para achar o que falta em cada parte, procure por `TODO (Pessoa N)` na IDE.

### Arquivos compartilhados

- **`Atendimento`** é o contrato entre as Pessoas 3 e 4. Qualquer mudança nele é combinada entre as duas.
- **`AtendimentoService`**: a Pessoa 3 é dona do arquivo; a Pessoa 4 preenche o caso `PROCEDIMENTO_CIRURGICO`
  em `agendar`, `iniciar`, `concluir` e `cancelarProcedimento`.
- **`buscarPorId`** de todos os services já está pronto, para ninguém ficar esperando o CRUD do outro.

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/pacientes` | Lista pacientes |
| GET | `/pacientes/{id}` | Busca paciente |
| POST | `/pacientes` | Cadastra paciente |
| PUT | `/pacientes/{id}` | Atualiza paciente |
| DELETE | `/pacientes/{id}` | Remove paciente |
| GET | `/profissionais?especialidade=` | Lista profissionais (filtro opcional por especialidade) |
| GET | `/profissionais/{id}` | Busca profissional |
| POST | `/profissionais` | Cadastra profissional (campo `tipo` define a categoria) |
| PUT | `/profissionais/{id}` | Atualiza profissional |
| DELETE | `/profissionais/{id}` | Remove profissional |
| GET | `/consultas` | Lista atendimentos de todas as modalidades |
| GET | `/consultas/{id}` | Busca atendimento |
| POST | `/consultas` | Agenda atendimento (campo `tipo` define a modalidade) |
| PATCH | `/consultas/{id}/situacao` | Altera a situação de uma consulta |
| PATCH | `/consultas/{id}/observacoes` | Registra observações |
| PATCH | `/consultas/{id}/iniciar` | Inicia emergência ou procedimento cirúrgico |
| PATCH | `/consultas/{id}/concluir` | Conclui emergência ou procedimento cirúrgico |
| PATCH | `/consultas/{id}/cancelar` | Cancela procedimento cirúrgico |
| GET | `/internacoes?ativas=` | Lista internações (filtro opcional das ainda sem alta) |
| GET | `/internacoes/{id}` | Busca internação |
| POST | `/internacoes` | Interna paciente |
| PATCH | `/internacoes/{id}/alta` | Dá alta |
| GET | `/quartos?tipo=&disponiveis=` | Lista quartos (filtros opcionais) |
| GET | `/quartos/{id}` | Busca quarto |
| POST | `/quartos` | Cadastra quarto |
| PUT | `/quartos/{id}` | Atualiza quarto |
| DELETE | `/quartos/{id}` | Remove quarto |

### Exemplos de corpo

`POST /profissionais`
```json
{ "tipo": "ESPECIALISTA", "nome": "Dra. Fernanda Lima", "registroProfissional": "CRM-MG 12345",
  "telefone": "31999990000", "email": "fernanda@hospital.com", "especialidade": "CARDIOLOGIA" }
```

`POST /consultas`
```json
{ "tipo": "ESPECIALIZADA", "pacienteId": 1, "profissionalId": 2, "data": "2026-11-20",
  "horario": "14:30", "motivo": "Dor no peito", "especialidade": "CARDIOLOGIA" }
```

`PATCH /consultas/1/situacao`
```json
{ "situacao": "CONFIRMADA" }
```

## Padrões combinados

- Regra de negócio violada: `throw new RegraNegocioException("mensagem")` → HTTP 422.
- Registro não encontrado: `throw new RecursoNaoEncontradoException("Paciente", id)` → HTTP 404.
- Campos inválidos (`@Valid`) → HTTP 400 com a lista de campos.
- Valores fixos (situação, prioridade, tipo...) sempre como `enum`, nunca texto livre.
- Uma branch por pessoa (`feat/pacientes`, `feat/profissionais`, `feat/consultas`, `feat/internacoes-quartos`),
  com merge na `main` após revisão de outro integrante.
