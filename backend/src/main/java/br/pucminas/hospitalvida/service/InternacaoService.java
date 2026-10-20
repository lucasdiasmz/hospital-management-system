package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.Requisicoes.InternacaoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.Internacao;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.ProfissionalSaude;
import br.pucminas.hospitalvida.model.Quarto;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;
import br.pucminas.hospitalvida.repository.InternacaoRepository;

@Service
@Transactional
public class InternacaoService {

    private final InternacaoRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;
    private final QuartoService quartoService;

    public InternacaoService(InternacaoRepository repository, PacienteService pacienteService,
                             ProfissionalService profissionalService, QuartoService quartoService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
        this.quartoService = quartoService;
    }

    @Transactional(readOnly = true)
    public List<Internacao> listar(StatusInternacao status, Long pacienteId) {
        return repository.findAll().stream()
                .filter(i -> status == null || i.getStatus() == status)
                .filter(i -> pacienteId == null || pacienteId.equals(i.getPaciente().getId()))
                .sorted(Comparator.comparing(Internacao::getDataEntrada).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public Internacao buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Internação", id));
    }

    public Internacao internar(InternacaoRequest req) {
        exigir(req.pacienteId() != null && req.profissionalId() != null && req.quartoId() != null,
                "Informe pacienteId, profissionalId e quartoId.");
        Paciente paciente = pacienteService.buscar(req.pacienteId());
        ProfissionalSaude profissional = profissionalService.buscar(req.profissionalId());
        Quarto quarto = quartoService.buscar(req.quartoId());

        Internacao internacao = new Internacao(paciente, profissional, quarto, req.dataPrevistaAlta(), req.observacoes());
        internacao.iniciar();
        paciente.adicionarInternacao(internacao);
        profissional.adicionarInternacao(internacao);
        return repository.save(internacao);
    }

    /** Se a data não for informada, considera hoje. */
    public Internacao registrarAlta(Long id, LocalDate dataAlta) {
        Internacao internacao = buscar(id);
        internacao.registrarAlta(dataAlta == null ? LocalDate.now() : dataAlta);
        return internacao;
    }

    public Internacao transferir(Long id, Long quartoId) {
        exigir(quartoId != null, "Informe o quartoId de destino.");
        Internacao internacao = buscar(id);
        internacao.transferir(quartoService.buscar(quartoId));
        return internacao;
    }

    public Internacao cancelar(Long id) {
        Internacao internacao = buscar(id);
        internacao.cancelar();
        return internacao;
    }
}
