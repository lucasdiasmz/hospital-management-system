package br.pucminas.hospitalvida.dto;

import java.time.LocalDateTime;

/** Formato padrão das respostas de erro da API. */
public record ErroResponse(int status, String erro, LocalDateTime timestamp) {

    public ErroResponse(int status, String erro) {
        this(status, erro, LocalDateTime.now());
    }
}
