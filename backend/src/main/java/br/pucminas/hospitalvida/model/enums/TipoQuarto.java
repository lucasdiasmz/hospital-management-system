package br.pucminas.hospitalvida.model.enums;

/**
 * Tipos de quarto e suas regras:
 *  - cada tipo tem um limite de leitos (capacidade máxima permitida);
 *  - internações em UTI e ISOLAMENTO exigem justificativa clínica (observações).
 */
public enum TipoQuarto {
    ENFERMARIA(6, false),
    APARTAMENTO(2, false),
    UTI(10, true),
    ISOLAMENTO(1, true);

    private final int limiteLeitos;
    private final boolean exigeJustificativa;

    TipoQuarto(int limiteLeitos, boolean exigeJustificativa) {
        this.limiteLeitos = limiteLeitos;
        this.exigeJustificativa = exigeJustificativa;
    }

    public int getLimiteLeitos() {
        return limiteLeitos;
    }

    public boolean exigeJustificativa() {
        return exigeJustificativa;
    }
}
