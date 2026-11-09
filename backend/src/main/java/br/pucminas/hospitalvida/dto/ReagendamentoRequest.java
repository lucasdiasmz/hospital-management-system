package br.pucminas.hospitalvida.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/** Corpo do POST /consultas/{id}/reagendamento. */
public record ReagendamentoRequest(LocalDate data, LocalTime horario) {
}
