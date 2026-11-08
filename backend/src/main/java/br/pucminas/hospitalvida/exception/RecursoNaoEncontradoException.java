package br.pucminas.hospitalvida.exception;

/**
 * Lançada quando um registro buscado pelo id não existe. Vira resposta HTTP 404.
 * Exemplo: repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " não encontrado");
    }
}
