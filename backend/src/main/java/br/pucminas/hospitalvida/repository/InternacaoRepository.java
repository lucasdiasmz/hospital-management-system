package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Internacao;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    boolean existsByQuarto_Id(Long quartoId);
}
