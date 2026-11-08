# Diagrama de Classes - Sprint 2

Mantido pela **Pessoa 1**. O GitHub renderiza o bloco Mermaid automaticamente; para exportar como
imagem, use https://mermaid.live.

| Parte | Situação |
|---|---|
| Pessoa 1 - Paciente | implementado |
| Pessoa 2 - Profissionais e encaminhamentos | implementado (branch `daniell`) |
| Pessoa 3 - Consultas e emergências | **previsto**, atualizar quando a branch chegar |
| Pessoa 4 - Procedimento cirúrgico, internações e quartos | **previsto**, atualizar quando a branch chegar |

## Implementado

```mermaid
classDiagram
    direction TB

    class Paciente {
        -Long id
        -String nome
        -String cpf
        -LocalDate dataNascimento
        -String telefone
        -String email
        -String endereco
    }

    class Profissional {
        <<abstract>>
        -Long id
        -String nome
        -String registroProfissional
        -String telefone
        -String email
        -boolean ativo
        +getCategoria()* CategoriaProfissional
        +podeRealizarConsultaRotina() boolean
        +atendeEspecialidade(Especialidade) boolean
        +podeRealizarCirurgia() boolean
        +atualizarNome(String) void
        +atualizarContato(String, String) void
        +desativar() void
        +reativar() void
        +exigirAtivo() void
    }
    class MedicoClinicoGeral {
        +podeRealizarConsultaRotina() boolean
        +encaminhar(Paciente, Especialidade, Profissional, String) Encaminhamento
    }
    class MedicoEspecialista {
        -Especialidade especialidade
        +atendeEspecialidade(Especialidade) boolean
    }
    class Cirurgiao {
        -String areaCirurgica
        -String rqe
        +podeRealizarCirurgia() boolean
        +atualizarAtuacaoCirurgica(String, String) void
    }
    Profissional <|-- MedicoClinicoGeral
    Profissional <|-- MedicoEspecialista
    Profissional <|-- Cirurgiao

    class Encaminhamento {
        -Long id
        -Especialidade especialidade
        -String motivo
        -LocalDateTime dataHora
    }
    MedicoClinicoGeral ..> Encaminhamento : cria
    Encaminhamento "*" --> "1" Paciente
    Encaminhamento "*" --> "1" MedicoClinicoGeral : clinico
    Encaminhamento "*" --> "0..1" Profissional : especialistaIndicado

    class CategoriaProfissional {
        <<enumeration>>
        CLINICO_GERAL
        ESPECIALISTA
        CIRURGIAO
    }
    class Especialidade {
        <<enumeration>>
        CARDIOLOGIA
        NEUROLOGIA
        ORTOPEDIA
        PEDIATRIA
        DERMATOLOGIA
        GINECOLOGIA
        ONCOLOGIA
        PSIQUIATRIA
    }
```

## Previsto (Pessoas 3 e 4)

Proposta inicial; será substituída pelas classes reais quando as branches forem integradas.

```mermaid
classDiagram
    direction TB

    class Paciente
    class Profissional

    class Atendimento {
        <<abstract>>
        -Long id
        -LocalDate data
        -LocalTime horario
        -String observacoes
        +validarRealizacao()* void
        +registrarObservacoes(String) void
    }
    class Consulta {
        <<abstract>>
        -String motivo
        -SituacaoConsulta situacao
        +alterarSituacao(SituacaoConsulta) void
    }
    class ConsultaRotina
    class ConsultaEspecializada {
        -Especialidade especialidade
    }
    class AtendimentoEmergencia {
        -Prioridade prioridade
        -StatusEmergencia status
        +iniciar() void
        +concluir() void
    }
    class ProcedimentoCirurgico {
        -String descricaoProcedimento
        -String tipoCirurgia
        -StatusProcedimento status
        +iniciar() void
        +concluir() void
        +cancelar() void
    }
    Atendimento <|-- Consulta
    Consulta <|-- ConsultaRotina
    Consulta <|-- ConsultaEspecializada
    Atendimento <|-- AtendimentoEmergencia
    Atendimento <|-- ProcedimentoCirurgico
    Atendimento "*" --> "1" Paciente
    Atendimento "*" --> "0..1" Profissional

    class Quarto {
        -Long id
        -String numero
        -TipoQuarto tipo
        -int capacidade
        -int leitosOcupados
        +temLeitoDisponivel() boolean
        +ocuparLeito() void
        +liberarLeito() void
    }
    class Internacao {
        -Long id
        -LocalDate dataEntrada
        -LocalDate dataAlta
        -String motivo
        +isAtiva() boolean
        +darAlta(LocalDate) void
    }
    Internacao "*" --> "1" Paciente
    Internacao "*" --> "1" Quarto
    Internacao "*" --> "1" Profissional : responsável
```
