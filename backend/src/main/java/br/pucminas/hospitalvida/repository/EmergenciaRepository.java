package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.AtendimentoEmergencia;

public interface EmergenciaRepository extends JpaRepository<AtendimentoEmergencia, Long> {
}
