package br.pucminas.hospitalvida.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.ConsultaRequest;
import br.pucminas.hospitalvida.dto.ReagendamentoRequest;
import br.pucminas.hospitalvida.dto.SituacaoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.Consulta;
import br.pucminas.hospitalvida.model.ConsultaEspecializada;
import br.pucminas.hospitalvida.model.ConsultaRotina;
import br.pucminas.hospitalvida.model.Encaminhamento;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.Profissional;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.repository.ConsultaRepository;
import br.pucminas.hospitalvida.repository.EncaminhamentoRepository;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository repository;
    private final EncaminhamentoRepository encaminhamentoRepository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public ConsultaService(ConsultaRepository repository, EncaminhamentoRepository encaminhamentoRepository,
                           PacienteService pacienteService, ProfissionalService profissionalService) {
        this.repository = repository;
        this.encaminhamentoRepository = encaminhamentoRepository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    /** Filtros opcionais. Ex.: GET /consultas?situacao=AGENDADA&profissionalId=1 */
    @Transactional(readOnly = true)
    public List<Consulta> listar(SituacaoConsulta situacao, Long pacienteId, Long profissionalId) {
        return repository.findAll().stream()
                .filter(c -> situacao == null || c.getSituacao() == situacao)
                .filter(c -> pacienteId == null || pacienteId.equals(c.getPaciente().getId()))
                .filter(c -> profissionalId == null || profissionalId.equals(c.getProfissional().getId()))
                .sorted(Comparator.comparing(Consulta::getData).thenComparing(Consulta::getHorario))
                .toList();
    }

    @Transactional(readOnly = true)
    public Consulta buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }

    /** Cria a subclasse certa de acordo com o tipo informado. */
    public Consulta agendar(ConsultaRequest req) {
        exigir(req.tipo() != null, "Informe o tipo da consulta: ROTINA ou ESPECIALIZADA.");
        exigir(req.pacienteId() != null, "Informe o pacienteId.");
        exigir(req.profissionalId() != null, "Informe o profissionalId.");
        Paciente paciente = pacienteService.buscarPorId(req.pacienteId());
        Profissional profissional = profissionalService.buscar(req.profissionalId());

        Consulta consulta = switch (req.tipo()) {
            case ROTINA -> new ConsultaRotina(req.data(), req.horario(), paciente, profissional,
                    req.motivo(), req.observacoes());
            case ESPECIALIZADA -> new ConsultaEspecializada(req.data(), req.horario(), paciente, profissional,
                    req.motivo(), req.observacoes(), req.especialidade(), buscarEncaminhamento(req.encaminhamentoId()));
        };
        consulta.validarAgendamento();
        return repository.save(consulta);
    }

    public Consulta alterarSituacao(Long id, SituacaoRequest req) {
        Consulta consulta = buscar(id);
        consulta.alterarSituacao(req.situacao(), req.observacoes());
        return consulta;
    }

    public Consulta registrarObservacoes(Long id, String texto) {
        Consulta consulta = buscar(id);
        consulta.registrarObservacoes(texto);
        return consulta;
    }

    /** Gera uma nova consulta a partir de uma cancelada ou com falta. A original continua como está. */
    public Consulta reagendar(Long id, ReagendamentoRequest req) {
        exigir(req.data() != null && req.horario() != null, "Informe a nova data e o novo horário.");
        Consulta original = buscar(id);
        exigir(!repository.existsByConsultaOriginal_Id(id), "Esta consulta já foi reagendada.");
        return repository.save(original.reagendar(req.data(), req.horario()));
    }

    private Encaminhamento buscarEncaminhamento(Long id) {
        if (id == null) {
            return null;
        }
        return encaminhamentoRepository.findById(id).orElseThrow(
                () -> new RecursoNaoEncontradoException("Encaminhamento", id));
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
