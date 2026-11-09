package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Internacao;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    boolean existsByQuarto_Id(Long quartoId);

    boolean existsByPaciente_IdAndStatus(Long pacienteId, StatusInternacao status);
}
