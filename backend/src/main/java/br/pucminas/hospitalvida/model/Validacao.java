package br.pucminas.hospitalvida.model;

import br.pucminas.hospitalvida.exception.RegraNegocioException;

/** Pequenos atalhos usados pelas classes de domínio para checar regras. */
public final class Validacao {

    private Validacao() {
    }

    /** Se a condição for falsa, lança RegraNegocioException com a mensagem. */
    public static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }

    public static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }
}
