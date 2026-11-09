package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Consulta;

/** Busca consultas de rotina e especializadas. */
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    boolean existsByConsultaOriginal_Id(Long consultaOriginalId);
}
