package br.pucminas.hospitalvida.model.enums;

/**
 * Situações possíveis de uma consulta (substitui o StatusConsulta da Sprint 1).
 *
 * Fluxo permitido:
 *   AGENDADA ──► CONFIRMADA ──► EM_ATENDIMENTO ──► CONCLUIDA
 *      │             │
 *      ├─► CANCELADA ◄┤
 *      └─► NAO_COMPARECEU ◄┘
 *
 * CONCLUIDA, CANCELADA e NAO_COMPARECEU são finais: nenhuma consulta volta
 * a ser AGENDADA. Uma consulta cancelada só é atendida por meio de um novo
 * agendamento (reagendamento), que gera outra consulta.
 */
public enum SituacaoConsulta {
    AGENDADA,
    CONFIRMADA,
    EM_ATENDIMENTO,
    CONCLUIDA,
    CANCELADA,
    NAO_COMPARECEU;

    public boolean podeMudarPara(SituacaoConsulta nova) {
        return switch (this) {
            case AGENDADA -> nova == CONFIRMADA || nova == CANCELADA || nova == NAO_COMPARECEU;
            case CONFIRMADA -> nova == EM_ATENDIMENTO || nova == CANCELADA || nova == NAO_COMPARECEU;
            case EM_ATENDIMENTO -> nova == CONCLUIDA;
            case CONCLUIDA, CANCELADA, NAO_COMPARECEU -> false;
        };
    }

    public boolean isFinal() {
        return this == CONCLUIDA || this == CANCELADA || this == NAO_COMPARECEU;
    }

    /** Consultas canceladas ou em que o paciente faltou não ocupam a agenda do médico. */
    public boolean ocupaAgenda() {
        return this != CANCELADA && this != NAO_COMPARECEU;
    }
}
