# Diagrama de Classes - Sprint 2

O GitHub renderiza os blocos Mermaid automaticamente; para exportar como imagem, use https://mermaid.live.
O modelo da Sprint 1 está em [`docs/sprint1/`](../../docs/sprint1/).

## Pessoas e profissionais

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
    Profissional ..> CategoriaProfissional
    MedicoEspecialista --> Especialidade
```

## Modalidades de atendimento

```mermaid
classDiagram
    direction TB

    class Paciente
    class Profissional
    class Encaminhamento

    class Atendimento {
        <<abstract>>
        -Long id
        -LocalDate data
        -LocalTime horario
        -String observacoes
        +validarRealizacao()* void
        +getTipo()* String
        #podeReceberObservacoes()* boolean
        +registrarObservacoes(String) void
    }
    class Consulta {
        <<abstract>>
        -String motivo
        -SituacaoConsulta situacao
        #criarCopia(LocalDate, LocalTime)* Consulta
        +validarAgendamento() void
        +alterarSituacao(SituacaoConsulta) void
        +reagendar(LocalDate, LocalTime) Consulta
    }
    class ConsultaRotina {
        +validarRealizacao() void
    }
    class ConsultaEspecializada {
        -Especialidade especialidade
        +validarRealizacao() void
    }
    class AtendimentoEmergencia {
        -Prioridade prioridade
        -StatusEmergencia status
        -String queixaPrincipal
        +validarRealizacao() void
        +iniciar(Profissional) void
        +concluir(String) void
    }
    class ProcedimentoCirurgico {
        -String descricao
        -TipoCirurgia tipoCirurgia
        -String observacoesPreOperatorias
        -String observacoesPosOperatorias
        -LocalDateTime inicioEfetivo
        -LocalDateTime fimEfetivo
        -StatusProcedimento status
        +validarRealizacao() void
        +validarAgendamento() void
        +iniciar() void
        +concluir(String) void
        +cancelar() void
    }
    Atendimento <|-- Consulta
    Consulta <|-- ConsultaRotina
    Consulta <|-- ConsultaEspecializada
    Atendimento <|-- AtendimentoEmergencia
    Atendimento <|-- ProcedimentoCirurgico
    Atendimento "*" --> "1" Paciente
    Atendimento "*" --> "0..1" Profissional : responsavel
    Consulta "0..1" --> "0..1" Consulta : consultaOriginal
    ConsultaEspecializada "*" --> "0..1" Encaminhamento

    class SituacaoConsulta {
        <<enumeration>>
        AGENDADA
        CONFIRMADA
        EM_ATENDIMENTO
        CONCLUIDA
        CANCELADA
        NAO_COMPARECEU
        +podeMudarPara(SituacaoConsulta) boolean
    }
    class Prioridade {
        <<enumeration>>
        BAIXA
        MODERADA
        ALTA
        CRITICA
    }
    class StatusEmergencia {
        <<enumeration>>
        AGUARDANDO
        INICIADO
        CONCLUIDO
        +podeMudarPara(StatusEmergencia) boolean
    }
    class TipoCirurgia {
        <<enumeration>>
        ELETIVA
        URGENCIA
        EMERGENCIA
    }
    class StatusProcedimento {
        <<enumeration>>
        AGENDADO
        EM_ANDAMENTO
        CONCLUIDO
        CANCELADO
        +podeMudarPara(StatusProcedimento) boolean
    }
    Consulta --> SituacaoConsulta
    AtendimentoEmergencia --> Prioridade
    AtendimentoEmergencia --> StatusEmergencia
    ProcedimentoCirurgico --> TipoCirurgia
    ProcedimentoCirurgico --> StatusProcedimento
```

## Internações e quartos

```mermaid
classDiagram
    direction TB

    class Paciente
    class Profissional

    class Quarto {
        -Long id
        -String numero
        -int andar
        -int capacidadeMaxima
        -int ocupacaoAtual
        -TipoQuarto tipo
        -SituacaoQuarto situacao
        +possuiVaga() boolean
        +ocupar() void
        +liberar() void
        +bloquear() void
        +desbloquear() void
    }
    class Internacao {
        -Long id
        -LocalDateTime dataEntrada
        -LocalDate dataPrevistaAlta
        -LocalDate dataEfetivaAlta
        -String observacoes
        -StatusInternacao status
        +iniciar() void
        +registrarAlta(LocalDate) void
        +cancelar() void
        +transferir(Quarto) void
        +estaAtiva() boolean
    }
    Internacao "*" --> "1" Paciente
    Internacao "*" --> "1" Profissional : responsavel
    Internacao "*" --> "1" Quarto

    class TipoQuarto {
        <<enumeration>>
        ENFERMARIA
        APARTAMENTO
        UTI
        ISOLAMENTO
        -int limiteLeitos
        -boolean exigeJustificativa
    }
    class SituacaoQuarto {
        <<enumeration>>
        DISPONIVEL
        PARCIALMENTE_OCUPADO
        LOTADO
        INDISPONIVEL
    }
    class StatusInternacao {
        <<enumeration>>
        ATIVA
        ENCERRADA
        CANCELADA
    }
    Quarto --> TipoQuarto
    Quarto --> SituacaoQuarto
    Internacao --> StatusInternacao
```
