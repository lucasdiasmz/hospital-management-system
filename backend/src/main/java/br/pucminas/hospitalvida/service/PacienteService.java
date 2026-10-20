package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.HistoricoResponse;
import br.pucminas.hospitalvida.dto.Requisicoes.PacienteRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.HistoricoMedico;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.repository.PacienteRepository;

@Service
@Transactional
public class PacienteService {

    private final PacienteRepository repository;

    public PacienteService(PacienteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Paciente> listar(String nome) {
        List<Paciente> pacientes = repository.findAll();
        if (nome != null && !nome.isBlank()) {
            String busca = nome.toLowerCase();
            pacientes = pacientes.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(busca))
                    .toList();
        }
        return pacientes;
    }

    @Transactional(readOnly = true)
    public Paciente buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    public Paciente cadastrar(PacienteRequest req) {
        Paciente paciente = new Paciente(req.nome(), req.telefone(), req.email(), req.cpf(),
                req.dataNascimento(), req.endereco());
        exigir(!repository.existsByCpf(paciente.getCpf()), "Já existe um paciente com o CPF " + paciente.getCpf() + ".");
        return repository.save(paciente);
    }

    /** CPF e data de nascimento não mudam; os demais dados podem ser atualizados. */
    public Paciente atualizar(Long id, PacienteRequest req) {
        Paciente paciente = buscar(id);
        paciente.atualizarNome(req.nome());
        paciente.atualizarContato(req.telefone(), req.email());
        paciente.atualizarEndereco(req.endereco());
        return paciente;
    }

    public void excluir(Long id) {
        Paciente paciente = buscar(id);
        exigir(!paciente.possuiRegistros(),
                "O paciente possui atendimentos ou internações registrados e não pode ser excluído.");
        repository.delete(paciente);
    }

    @Transactional(readOnly = true)
    public HistoricoResponse historico(Long id) {
        Paciente paciente = buscar(id);
        HistoricoMedico historico = paciente.getHistoricoMedico();
        return new HistoricoResponse(paciente, historico.getInformacoesRelevantes(),
                historico.listarAtendimentos(), historico.listarInternacoes());
    }

    public HistoricoResponse atualizarInformacoesHistorico(Long id, String informacoes) {
        buscar(id).getHistoricoMedico().atualizarInformacoes(informacoes);
        return historico(id);
    }
}
