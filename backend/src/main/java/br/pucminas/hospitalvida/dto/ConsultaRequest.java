package br.pucminas.hospitalvida.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.TipoConsulta;

/**
 * Corpo do POST /consultas.
 *   - especialidade e encaminhamentoId: só para ESPECIALIZADA (encaminhamentoId é opcional)
 *   - observacoes: opcional
 */
public record ConsultaRequest(TipoConsulta tipo, LocalDate data, LocalTime horario, Long pacienteId,
                              Long profissionalId, String motivo, String observacoes,
                              Especialidade especialidade, Long encaminhamentoId) {
}
