# Pessoa 2 — Profissionais de saúde

Responsável: Daniell · Branch: `daniell`

## O que tem

| Arquivo | O que é |
|---|---|
| `model/Profissional.java` | Classe **abstrata**: nome, registro profissional (único), telefone, e-mail e se está ativo |
| `model/MedicoClinicoGeral.java` | Faz consulta de rotina e **encaminha** pacientes para especialistas |
| `model/MedicoEspecialista.java` | Guarda a `especialidade` |
| `model/Cirurgiao.java` | Guarda a atuação cirúrgica: `areaCirurgica` e `rqe` |
| `model/Encaminhamento.java` | Registro do encaminhamento (paciente, clínico, especialidade, especialista indicado, motivo, data) |
| `model/enums/Especialidade.java` | CARDIOLOGIA, NEUROLOGIA, ORTOPEDIA, PEDIATRIA, DERMATOLOGIA, GINECOLOGIA, ONCOLOGIA, PSIQUIATRIA |
| `model/enums/CategoriaProfissional.java` | CLINICO_GERAL, ESPECIALISTA, CIRURGIAO |
| `repository/`, `service/`, `controller/`, `dto/` | Endpoint `/profissionais` |

As três categorias ficam na **mesma tabela** (`@Inheritance(SINGLE_TABLE)`), com a coluna `categoria`.

## Para as Pessoas 3 e 4: use estes métodos

Não testem o tipo do profissional com `instanceof`. Perguntem pra ele o que ele pode fazer (polimorfismo):

```java
profissional.podeRealizarConsultaRotina()          // true só para MedicoClinicoGeral   -> ConsultaRotina
profissional.atendeEspecialidade(Especialidade.X)  // true só para o especialista de X  -> ConsultaEspecializada
profissional.podeRealizarCirurgia()                // true só para Cirurgiao            -> ProcedimentoCirurgico
profissional.exigirAtivo()                         // lança RegraNegocioException se estiver desativado
```

Exemplo no `validarRealizacao()` da ConsultaEspecializada:

```java
if (!profissional.atendeEspecialidade(especialidade)) {
    throw new RegraNegocioException("O profissional não atende a especialidade " + especialidade + ".");
}
```

Para buscar um profissional pelo id: `profissionalService.buscar(id)` (devolve 404 se não existir).

## Regras implementadas

- Registro profissional obrigatório e único; nome obrigatório; e-mail opcional, mas com formato válido.
- Especialista precisa de especialidade; cirurgião precisa de área cirúrgica e RQE.
- Só o clínico geral encaminha. O especialista indicado (opcional) precisa atender a especialidade do encaminhamento, e ninguém desativado participa.
- Categoria, especialidade e registro não mudam depois do cadastro.
- Profissional não é apagado, é **desativado**: sai das buscas, mas o histórico dos atendimentos continua apontando pra ele.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| GET | `/profissionais?categoria=&especialidade=&incluirInativos=` | lista (por padrão, só os ativos) |
| GET | `/profissionais/{id}` | busca |
| POST | `/profissionais` | cadastra (o campo `categoria` define a subclasse) |
| PUT | `/profissionais/{id}` | atualiza nome, contato e atuação cirúrgica |
| PATCH | `/profissionais/{id}/desativar` · `/reativar` | tira ou volta para as buscas |
| POST | `/profissionais/{id}/encaminhamentos` | clínico `{id}` encaminha um paciente |
| GET | `/profissionais/{id}/encaminhamentos` | encaminhamentos feitos pelo clínico |
| GET | `/profissionais/{id}/procedimentos` | procedimentos do cirurgião (data, paciente e descrição) |

```json
POST /profissionais
{ "categoria": "ESPECIALISTA", "nome": "Dra. Beatriz Rocha", "registroProfissional": "CRM-MG 20002",
  "telefone": "(31) 3333-0002", "email": "beatriz@hospitalvida.com", "especialidade": "CARDIOLOGIA" }

POST /profissionais
{ "categoria": "CIRURGIAO", "nome": "Dr. Rafael Costa", "registroProfissional": "CRM-MG 30004",
  "areaCirurgica": "Cirurgia geral", "rqe": "RQE 12345" }

POST /profissionais/1/encaminhamentos
{ "pacienteId": 1, "especialidade": "CARDIOLOGIA", "especialistaId": 2, "motivo": "Sopro no coração" }
```

## O que vem da Pessoa 1

A minha parte usa três coisas da base:
- `exception/RegraNegocioException` (com construtor `RegraNegocioException(String mensagem)`), que o `@ControllerAdvice` transforma em 422
- `model/Paciente` (usado no encaminhamento)
- `repository/PacienteRepository`

No `ProfissionalTest`, o método `paciente()` cria um `new Paciente()`. Se o construtor da Pessoa 1 for diferente, é só ajustar essa linha.
