package br.pucminas.hospitalvida.exception;

import java.util.Arrays;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import br.pucminas.hospitalvida.dto.ErroResponse;

/**
 * Converte as exceções em respostas HTTP com mensagem clara.
 * Assim os services e o domínio só lançam a exceção, sem tratar nada.
 */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /** JSON malformado ou valor de enum que não existe (ex.: "situacao": "finalizada"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex) {
        String mensagem = "JSON inválido. Verifique os campos e o formato das datas (ex.: 2026-10-20T14:30:00).";
        if (ex.getCause() instanceof InvalidFormatException formato && formato.getTargetType().isEnum()) {
            mensagem = "Valor '" + formato.getValue() + "' não é permitido. Valores aceitos: "
                    + Arrays.toString(formato.getTargetType().getEnumConstants());
        }
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    /** Parâmetro de URL com tipo errado (ex.: ?situacao=xyz ou /pacientes/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        String mensagem = "Valor '" + ex.getValue() + "' inválido para o parâmetro '" + ex.getName() + "'.";
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            mensagem += " Valores aceitos: " + Arrays.toString(ex.getRequiredType().getEnumConstants());
        }
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex) {
        return resposta(HttpStatus.CONFLICT, "Operação viola a integridade dos dados (registro duplicado ou em uso).");
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(status.value(), mensagem));
    }
}
