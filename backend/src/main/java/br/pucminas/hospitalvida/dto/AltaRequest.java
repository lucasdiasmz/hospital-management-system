package br.pucminas.hospitalvida.dto;

import java.time.LocalDate;

/** Corpo opcional do PATCH /internacoes/{id}/alta. Sem ele, a alta é registrada com a data de hoje. */
public record AltaRequest(LocalDate dataAlta) {
}
