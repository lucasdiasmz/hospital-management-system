package br.pucminas.hospitalvida.exception;

/**
 * Lançada quando uma regra de negócio é violada. Vira resposta HTTP 422.
 * Exemplo: throw new RegraNegocioException("Consulta concluída não pode voltar a ser agendada");
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
