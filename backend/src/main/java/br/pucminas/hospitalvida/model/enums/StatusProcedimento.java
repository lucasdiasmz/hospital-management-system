package br.pucminas.hospitalvida.model.enums;

/**
 * AGENDADO ──► EM_ANDAMENTO ──► CONCLUIDO
 *    └──► CANCELADO
 *
 * Um procedimento só pode ser CONCLUIDO se estiver EM_ANDAMENTO,
 * ou seja, se tiver sido iniciado.
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
