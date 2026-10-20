package br.pucminas.hospitalvida.model.enums;

/** Classificação de prioridade do atendimento de emergência. */
public enum Prioridade {
    BAIXA(1),
    MODERADA(2),
    ALTA(3),
    CRITICA(4);

    private final int nivel;

    Prioridade(int nivel) {
        this.nivel = nivel;
    }

    /** Quanto maior o nível, mais urgente. Usado para ordenar a fila. */
    public int getNivel() {
        return nivel;
    }
}
