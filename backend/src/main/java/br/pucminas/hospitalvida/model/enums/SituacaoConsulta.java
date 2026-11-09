package br.pucminas.hospitalvida.model.enums;

/**
 * Situações de uma consulta e as transições permitidas entre elas:
 *   AGENDADA       -> CONFIRMADA, CANCELADA, NAO_COMPARECEU
 *   CONFIRMADA     -> EM_ATENDIMENTO, CANCELADA, NAO_COMPARECEU
 *   EM_ATENDIMENTO -> CONCLUIDA
 *
 * CONCLUIDA, CANCELADA e NAO_COMPARECEU são finais. Para atender um paciente depois de
 * um cancelamento ou falta, cria-se um novo agendamento (Consulta.reagendar).
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
}
