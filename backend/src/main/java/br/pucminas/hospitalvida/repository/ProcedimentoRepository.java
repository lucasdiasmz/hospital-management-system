package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;

public interface ProcedimentoRepository extends JpaRepository<ProcedimentoCirurgico, Long> {
}
