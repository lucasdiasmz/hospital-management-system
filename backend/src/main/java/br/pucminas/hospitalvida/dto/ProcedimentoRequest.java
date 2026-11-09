package br.pucminas.hospitalvida.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospitalvida.model.enums.TipoCirurgia;

/** Corpo do POST /consultas/procedimentos. data e horario são os previstos. */
public record ProcedimentoRequest(LocalDate data, LocalTime horario, Long pacienteId, Long cirurgiaoId,
                                  String descricao, TipoCirurgia tipoCirurgia, String observacoesPreOperatorias) {
}
