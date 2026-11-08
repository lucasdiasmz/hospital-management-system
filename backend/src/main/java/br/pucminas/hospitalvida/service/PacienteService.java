package br.pucminas.hospitalvida.service;

import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Responsável: Pessoa 1.
 * buscarPorId é usado pelos outros services para obter o paciente (404 se não existir).
 */
@Service
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    public Paciente buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public List<Paciente> listar() {
        return repository.findAll();
    }

    public Paciente cadastrar(Paciente paciente) {
        paciente.setId(null);
        return salvar(paciente);
    }

    public Paciente atualizar(Long id, Paciente dados) {
        buscarPorId(id);
        dados.setId(id);
        return salvar(dados);
    }

    /**
     * Paciente com consultas, internações ou encaminhamentos não é apagado, para não perder o histórico:
     * o banco recusa a exclusão e o GlobalExceptionHandler responde 409.
     */
    public void remover(Long id) {
        repository.delete(buscarPorId(id));
    }

    private Paciente salvar(Paciente paciente) {
        paciente.setCpf(paciente.getCpf().replaceAll("\\D", ""));
        repository.findByCpf(paciente.getCpf())
                .filter(outro -> !outro.getId().equals(paciente.getId()))
                .ifPresent(outro -> {
                    throw new RegraNegocioException("Já existe paciente cadastrado com o CPF " + paciente.getCpf());
                });
        return repository.save(paciente);
    }
}
