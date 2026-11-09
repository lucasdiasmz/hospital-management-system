package br.pucminas.hospitalvida.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.EmergenciaRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.AtendimentoEmergencia;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import br.pucminas.hospitalvida.repository.EmergenciaRepository;

@Service
@Transactional
public class EmergenciaService {

    /** Mais urgente primeiro; no empate, quem chegou antes. */
    private static final Comparator<AtendimentoEmergencia> ORDEM_DA_FILA =
            Comparator.comparing(AtendimentoEmergencia::getPrioridade).reversed()
                    .thenComparing(AtendimentoEmergencia::getData)
                    .thenComparing(AtendimentoEmergencia::getHorario);

    private final EmergenciaRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public EmergenciaService(EmergenciaRepository repository, PacienteService pacienteService,
                             ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    /** Ex.: ?status=AGUARDANDO mostra quem ainda aguarda avaliação. */
    @Transactional(readOnly = true)
    public List<AtendimentoEmergencia> listar(StatusEmergencia status) {
        return repository.findAll().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .sorted(ORDEM_DA_FILA)
                .toList();
    }

    @Transactional(readOnly = true)
    public AtendimentoEmergencia buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Atendimento de emergência", id));
    }

    public AtendimentoEmergencia registrar(EmergenciaRequest req) {
        exigir(req.pacienteId() != null, "Informe o pacienteId.");
        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(
                pacienteService.buscarPorId(req.pacienteId()), req.prioridade(), req.queixaPrincipal());
        emergencia.validarRealizacao();
        return repository.save(emergencia);
    }

    public AtendimentoEmergencia iniciar(Long id, Long profissionalId) {
        exigir(profissionalId != null, "Informe o profissionalId do médico que vai avaliar.");
        AtendimentoEmergencia emergencia = buscar(id);
        emergencia.iniciar(profissionalService.buscar(profissionalId));
        return emergencia;
    }

    public AtendimentoEmergencia concluir(Long id, String observacoes) {
        AtendimentoEmergencia emergencia = buscar(id);
        emergencia.concluir(observacoes);
        return emergencia;
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
