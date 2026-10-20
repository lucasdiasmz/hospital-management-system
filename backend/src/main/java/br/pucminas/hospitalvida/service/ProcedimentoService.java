package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.Requisicoes.ProcedimentoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.Cirurgiao;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;
import br.pucminas.hospitalvida.model.enums.StatusProcedimento;
import br.pucminas.hospitalvida.repository.ProcedimentoRepository;

@Service
@Transactional
public class ProcedimentoService {

    private final ProcedimentoRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public ProcedimentoService(ProcedimentoRepository repository, PacienteService pacienteService,
                               ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoCirurgico> listar(StatusProcedimento status, Long cirurgiaoId, Long pacienteId) {
        return repository.findAll().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> cirurgiaoId == null || cirurgiaoId.equals(p.getProfissional().getId()))
                .filter(p -> pacienteId == null || pacienteId.equals(p.getPaciente().getId()))
                .sorted(Comparator.comparing(ProcedimentoCirurgico::getDataHoraPrevista))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoCirurgico buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Procedimento cirúrgico", id));
    }

    public ProcedimentoCirurgico agendar(ProcedimentoRequest req) {
        exigir(req.pacienteId() != null && req.cirurgiaoId() != null, "Informe pacienteId e cirurgiaoId.");
        Paciente paciente = pacienteService.buscar(req.pacienteId());
        Cirurgiao cirurgiao = profissionalService.buscarCirurgiao(req.cirurgiaoId());
        int duracao = req.duracaoPrevistaMinutos() == null ? 0 : req.duracaoPrevistaMinutos();

        ProcedimentoCirurgico procedimento = cirurgiao.registrarProcedimento(paciente, req.descricao(),
                req.tipoCirurgia(), req.dataHoraPrevista(), duracao, req.observacoesPreOperatorias());
        procedimento.validar();
        exigir(cirurgiao.estaDisponivel(procedimento.getDataHora(), procedimento.getDataHoraFimPrevista()),
                "O cirurgião " + cirurgiao.getNome() + " já tem um atendimento nesse horário.");

        paciente.adicionarAtendimento(procedimento);
        cirurgiao.adicionarAtendimento(procedimento);
        return repository.save(procedimento);
    }

    public ProcedimentoCirurgico iniciar(Long id) {
        ProcedimentoCirurgico procedimento = buscar(id);
        procedimento.iniciar();
        return procedimento;
    }

    public ProcedimentoCirurgico concluir(Long id, String observacoesPosOperatorias) {
        ProcedimentoCirurgico procedimento = buscar(id);
        procedimento.concluir(observacoesPosOperatorias);
        return procedimento;
    }

    public ProcedimentoCirurgico cancelar(Long id) {
        ProcedimentoCirurgico procedimento = buscar(id);
        procedimento.cancelar();
        return procedimento;
    }
}
