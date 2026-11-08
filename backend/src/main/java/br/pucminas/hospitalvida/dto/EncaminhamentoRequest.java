package br.pucminas.hospitalvida.dto;

import br.pucminas.hospitalvida.model.enums.Especialidade;

/** Corpo do POST /profissionais/{idClinico}/encaminhamentos. especialistaId é opcional. */
public record EncaminhamentoRequest(Long pacienteId, Especialidade especialidade, Long especialistaId,
                                    String motivo) {
}
