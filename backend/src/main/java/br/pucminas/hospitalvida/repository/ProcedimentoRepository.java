package br.pucminas.hospitalvida.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;

public interface ProcedimentoRepository extends JpaRepository<ProcedimentoCirurgico, Long> {

    List<ProcedimentoCirurgico> findByProfissional_Id(Long cirurgiaoId);
}
