package br.pucminas.hospitalvida.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.ProcedimentoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
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

    /** Filtros opcionais. Ex.: ?status=AGENDADO&cirurgiaoId=3 */
    @Transactional(readOnly = true)
    public List<ProcedimentoCirurgico> listar(StatusProcedimento status, Long cirurgiaoId, Long pacienteId) {
        return repository.findAll().stream()
                .filter(p -> status == null || p.getStatus() == status)
                .filter(p -> cirurgiaoId == null || cirurgiaoId.equals(p.getProfissional().getId()))
                .filter(p -> pacienteId == null || pacienteId.equals(p.getPaciente().getId()))
                .sorted(Comparator.comparing(ProcedimentoCirurgico::getData)
                        .thenComparing(ProcedimentoCirurgico::getHorario))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProcedimentoCirurgico buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Procedimento cirúrgico", id));
    }

    public ProcedimentoCirurgico agendar(ProcedimentoRequest req) {
        exigir(req.pacienteId() != null, "Informe o pacienteId.");
        exigir(req.cirurgiaoId() != null, "Informe o cirurgiaoId.");
        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(req.data(), req.horario(),
                pacienteService.buscarPorId(req.pacienteId()), profissionalService.buscar(req.cirurgiaoId()),
                req.descricao(), req.tipoCirurgia(), req.observacoesPreOperatorias());
        procedimento.validarAgendamento();
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

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
