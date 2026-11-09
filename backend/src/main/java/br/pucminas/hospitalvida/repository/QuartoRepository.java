package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Quarto;

public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    boolean existsByNumero(String numero);
}
