package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.ProfissionalSaude;

public interface ProfissionalRepository extends JpaRepository<ProfissionalSaude, Long> {

    boolean existsByRegistroProfissional(String registroProfissional);
}
