package br.pucminas.hospitalvida.model.enums;

/** Andamento do atendimento de emergência: AGUARDANDO -> INICIADO -> CONCLUIDO. */
public enum StatusEmergencia {
    AGUARDANDO,
    INICIADO,
    CONCLUIDO;

    public boolean podeMudarPara(StatusEmergencia novo) {
        return switch (this) {
            case AGUARDANDO -> novo == INICIADO;
            case INICIADO -> novo == CONCLUIDO;
            case CONCLUIDO -> false;
        };
    }
}
