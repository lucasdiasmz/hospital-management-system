package br.pucminas.hospitalvida.exception;

import java.time.LocalDateTime;

/** Corpo padrão das respostas de erro da API. */
public record ErroResponse(int status, String mensagem, LocalDateTime momento) {

    public ErroResponse(int status, String mensagem) {
        this(status, mensagem, LocalDateTime.now());
    }
}
