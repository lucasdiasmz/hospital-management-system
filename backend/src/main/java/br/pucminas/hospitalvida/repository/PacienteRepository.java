package br.pucminas.hospitalvida.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.pucminas.hospitalvida.model.Paciente;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);
}
