package br.pucminas.hospitalvida.dto;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;

/**
 * Corpo do POST e do PUT de /profissionais.
 *   - especialidade: só para ESPECIALISTA
 *   - areaCirurgica e rqe: só para CIRURGIAO
 */
public record ProfissionalRequest(CategoriaProfissional categoria, String nome, String registroProfissional,
                                  String telefone, String email, Especialidade especialidade,
                                  String areaCirurgica, String rqe) {
}
