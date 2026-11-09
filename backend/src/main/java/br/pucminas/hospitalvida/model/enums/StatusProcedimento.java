package br.pucminas.hospitalvida.model.enums;

/**
 * Andamento do procedimento cirúrgico:
 *   AGENDADO     -> EM_ANDAMENTO, CANCELADO
 *   EM_ANDAMENTO -> CONCLUIDO
 *
 * Um procedimento só chega a CONCLUIDO se tiver sido iniciado.
 */
public enum StatusProcedimento {
    AGENDADO,
    EM_ANDAMENTO,
    CONCLUIDO,
    CANCELADO;

    public boolean podeMudarPara(StatusProcedimento novo) {
        return switch (this) {
            case AGENDADO -> novo == EM_ANDAMENTO || novo == CANCELADO;
            case EM_ANDAMENTO -> novo == CONCLUIDO;
            case CONCLUIDO, CANCELADO -> false;
        };
    }
}
