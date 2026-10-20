package br.pucminas.hospitalvida.exception;

/** Lançada quando um id informado não existe. A API responde com HTTP 404. */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " não encontrado(a).");
    }
}
