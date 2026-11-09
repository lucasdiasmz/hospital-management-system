# Pessoa 3 — Consultas e situações

Responsável: Matheus · Branch: `feat/consultas`

## O que tem

| Arquivo | O que é |
|---|---|
| `model/Atendimento.java` | Classe **abstrata**: data, horário, paciente, profissional, observações e `validarRealizacao()` |
| `model/Consulta.java` | Classe **abstrata**: motivo e situação, comuns às consultas |
| `model/ConsultaRotina.java` | Só aceita clínico geral |
| `model/ConsultaEspecializada.java` | Guarda a especialidade e só aceita profissional compatível |
| `model/AtendimentoEmergencia.java` | Guarda prioridade, status e queixa principal |
| `model/enums/SituacaoConsulta.java` | Situações e transições permitidas |
| `model/enums/Prioridade.java` | BAIXA, MODERADA, ALTA, CRITICA |
| `model/enums/StatusEmergencia.java` | AGUARDANDO, INICIADO, CONCLUIDO |

As modalidades ficam na **mesma tabela** (`@Inheritance(SINGLE_TABLE)`), com a coluna `tipo_atendimento`.

## Como estender Atendimento

`ProcedimentoCirurgico` (Pessoa 4) é o exemplo: uma nova modalidade precisa de `@Entity`, `@DiscriminatorValue`
e de `validarRealizacao()`, `getTipo()` e `podeReceberObservacoes()`. Como a tabela é compartilhada, cada
coluna de status tem nome próprio (`status_emergencia`, `status_procedimento`).

## Regras implementadas

- A consulta nasce `AGENDADA` e só pode ser marcada para o futuro.
- Transições: `AGENDADA → CONFIRMADA → EM_ATENDIMENTO → CONCLUIDA`. Antes de começar o atendimento, também pode ir para `CANCELADA` ou `NAO_COMPARECEU`.
- `CONCLUIDA`, `CANCELADA` e `NAO_COMPARECEU` são finais. Consulta cancelada ou com falta só volta por um novo agendamento (uma única vez).
- Ao entrar em `EM_ATENDIMENTO`, `validarRealizacao()` roda de novo (ex.: o médico pode ter sido desativado).
- Observações clínicas só a partir de `EM_ATENDIMENTO`. O POST aceita uma observação inicial, como na tela de nova consulta.
- O encaminhamento (opcional) precisa ser do mesmo paciente e da mesma especialidade da consulta.
- Emergência: `AGUARDANDO → INICIADO → CONCLUIDO`. O médico entra só ao iniciar; a fila sai da prioridade mais alta para a mais baixa.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| GET | `/consultas?situacao=&pacienteId=&profissionalId=` | lista |
| GET | `/consultas/{id}` | busca |
| POST | `/consultas` | agenda (o campo `tipo` define a subclasse) |
| PATCH | `/consultas/{id}/situacao` | troca a situação |
| PATCH | `/consultas/{id}/observacoes` | registra observações |
| POST | `/consultas/{id}/reagendamento` | novo agendamento de consulta cancelada ou com falta |
| GET | `/consultas/emergencias?status=` | lista a fila de emergência |
| GET | `/consultas/emergencias/{id}` | busca |
| POST | `/consultas/emergencias` | registra a chegada do paciente |
| PATCH | `/consultas/emergencias/{id}/iniciar` | médico assume o caso |
| PATCH | `/consultas/emergencias/{id}/concluir` | conclui, com observações opcionais |

```json
POST /consultas
{ "tipo": "ESPECIALIZADA", "data": "2026-12-01", "horario": "10:00", "pacienteId": 1, "profissionalId": 2,
  "motivo": "Sopro no coração", "especialidade": "CARDIOLOGIA", "encaminhamentoId": 1 }

PATCH /consultas/1/situacao
{ "situacao": "CONCLUIDA", "observacoes": "Retorno em 6 meses" }

POST /consultas/emergencias
{ "pacienteId": 1, "prioridade": "CRITICA", "queixaPrincipal": "Dor no peito" }
```
