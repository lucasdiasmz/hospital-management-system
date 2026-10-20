package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.Requisicoes.EmergenciaRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.AtendimentoEmergencia;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.ProfissionalSaude;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import br.pucminas.hospitalvida.repository.EmergenciaRepository;

@Service
@Transactional
public class EmergenciaService {

    /** Fila: mais urgente primeiro; empate, quem chegou antes. */
    private static final Comparator<AtendimentoEmergencia> ORDEM_DA_FILA =
            Comparator.comparing((AtendimentoEmergencia e) -> e.getPrioridade().getNivel()).reversed()
                    .thenComparing(AtendimentoEmergencia::getDataHora);

    private final EmergenciaRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public EmergenciaService(EmergenciaRepository repository, PacienteService pacienteService,
                             ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    /** Ex.: ?status=AGUARDANDO_AVALIACAO mostra a fila de quem ainda não foi avaliado. */
    @Transactional(readOnly = true)
    public List<AtendimentoEmergencia> listar(StatusEmergencia status) {
        return repository.findAll().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .sorted(ORDEM_DA_FILA)
                .toList();
    }

    @Transactional(readOnly = true)
    public AtendimentoEmergencia buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Atendimento de emergência", id));
    }

    public AtendimentoEmergencia registrar(EmergenciaRequest req) {
        exigir(req.pacienteId() != null, "Informe o pacienteId.");
        Paciente paciente = pacienteService.buscar(req.pacienteId());
        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(paciente, req.prioridade(), req.queixaPrincipal());
        emergencia.validar();
        paciente.adicionarAtendimento(emergencia);
        return repository.save(emergencia);
    }

    public AtendimentoEmergencia iniciarAvaliacao(Long id, Long profissionalId) {
        exigir(profissionalId != null, "Informe o profissionalId do médico que vai avaliar.");
        AtendimentoEmergencia emergencia = buscar(id);
        ProfissionalSaude medico = profissionalService.buscar(profissionalId);
        emergencia.iniciarAvaliacao(medico);
        medico.adicionarAtendimento(emergencia);
        return emergencia;
    }

    public AtendimentoEmergencia concluir(Long id, String observacoes) {
        AtendimentoEmergencia emergencia = buscar(id);
        emergencia.concluir(observacoes);
        return emergencia;
    }
}
