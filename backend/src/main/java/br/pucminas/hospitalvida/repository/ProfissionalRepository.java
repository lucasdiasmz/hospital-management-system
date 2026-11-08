package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Profissional;

/** Busca qualquer categoria de profissional (a tabela é única para as três). */
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    boolean existsByRegistroProfissional(String registroProfissional);
}
