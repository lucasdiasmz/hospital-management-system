package br.pucminas.hospitalvida.dto;

import br.pucminas.hospitalvida.model.enums.TipoQuarto;

/** Corpo do POST /quartos. */
public record QuartoRequest(String numero, Integer andar, TipoQuarto tipo, Integer capacidadeMaxima) {
}
