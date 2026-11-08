package br.pucminas.hospitalvida.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Encaminhamento;

public interface EncaminhamentoRepository extends JpaRepository<Encaminhamento, Long> {

    List<Encaminhamento> findByClinico_Id(Long clinicoId);
}
