package br.pucminas.hospitalvida.dto;

/** Corpo do PATCH /consultas/{id}/observacoes e do PATCH /consultas/emergencias/{id}/concluir. */
public record ObservacaoRequest(String texto) {
}
