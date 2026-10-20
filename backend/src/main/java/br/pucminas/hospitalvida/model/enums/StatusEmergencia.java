package br.pucminas.hospitalvida.model.enums;

/** AGUARDANDO_AVALIACAO ──► EM_ATENDIMENTO ──► CONCLUIDO */
public enum StatusEmergencia {
    AGUARDANDO_AVALIACAO,
    EM_ATENDIMENTO,
    CONCLUIDO;

    public boolean podeMudarPara(StatusEmergencia novo) {
        return switch (this) {
            case AGUARDANDO_AVALIACAO -> novo == EM_ATENDIMENTO;
            case EM_ATENDIMENTO -> novo == CONCLUIDO;
            case CONCLUIDO -> false;
        };
    }
}
