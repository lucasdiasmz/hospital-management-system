package br.pucminas.hospitalvida.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio do hospital
 * (ex.: concluir uma consulta cancelada, ocupar um quarto lotado).
 * A API responde com HTTP 422.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
