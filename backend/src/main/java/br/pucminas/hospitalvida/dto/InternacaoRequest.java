package br.pucminas.hospitalvida.dto;

import java.time.LocalDate;

/** Corpo do POST /internacoes. dataPrevistaAlta é opcional; observacoes é obrigatória em UTI e isolamento. */
public record InternacaoRequest(Long pacienteId, Long profissionalId, Long quartoId, LocalDate dataPrevistaAlta,
                                String observacoes) {
}
