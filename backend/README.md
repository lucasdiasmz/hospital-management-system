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
└── exception/      exceções de negócio e tratamento global de erros
docs/diagrama-classes.md   diagrama de classes (Mermaid)
```

## Divisão das tarefas

| Pessoa | Parte | Situação |
|---|---|---|
| 1 | Base do projeto, tratamento de erros, pacientes, diagrama, integração | pronto |
| 2 | Profissionais de saúde e encaminhamentos (`docs/pessoa2-profissionais.md`) | integrado |
| 3 | Consultas, emergências e situações | pendente |
| 4 | Procedimento cirúrgico, internações e quartos | pendente |

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
