package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Consulta;

/** Busca consultas de rotina e especializadas (as duas são Consulta). */
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
}
