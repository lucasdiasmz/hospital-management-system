package br.pucminas.hospitalvida.dto;

import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;

/** Corpo do PATCH /consultas/{id}/situacao. observacoes é opcional. */
public record SituacaoRequest(SituacaoConsulta situacao, String observacoes) {
}
