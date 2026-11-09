package br.pucminas.hospitalvida.dto;

import br.pucminas.hospitalvida.model.enums.Prioridade;

/** Corpo do POST /consultas/emergencias. */
public record EmergenciaRequest(Long pacienteId, Prioridade prioridade, String queixaPrincipal) {
}
