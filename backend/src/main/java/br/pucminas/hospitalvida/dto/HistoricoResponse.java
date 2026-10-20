package br.pucminas.hospitalvida.dto;

import java.util.List;

import br.pucminas.hospitalvida.model.Atendimento;
import br.pucminas.hospitalvida.model.Internacao;
import br.pucminas.hospitalvida.model.Paciente;

/** Resposta de GET /pacientes/{id}/historico (usada pela tela de Histórico do front). */
public record HistoricoResponse(Paciente paciente, String informacoesRelevantes,
                                List<Atendimento> atendimentos, List<Internacao> internacoes) {
}
