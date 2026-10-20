package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.Requisicoes.ConsultaRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.MudancaSituacaoRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.ObservacaoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.Consulta;
import br.pucminas.hospitalvida.model.ConsultaEspecializada;
import br.pucminas.hospitalvida.model.ConsultaRotina;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.ProfissionalSaude;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.repository.ConsultaRepository;

@Service
@Transactional
public class ConsultaService {

    private final ConsultaRepository repository;
    private final PacienteService pacienteService;
    private final ProfissionalService profissionalService;

    public ConsultaService(ConsultaRepository repository, PacienteService pacienteService,
                           ProfissionalService profissionalService) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.profissionalService = profissionalService;
    }

    @Transactional(readOnly = true)
    public List<Consulta> listar(SituacaoConsulta situacao, Long pacienteId, Long profissionalId) {
        return repository.findAll().stream()
                .filter(c -> situacao == null || c.getSituacao() == situacao)
                .filter(c -> pacienteId == null || pacienteId.equals(c.getPaciente().getId()))
                .filter(c -> profissionalId == null || profissionalId.equals(c.getProfissional().getId()))
                .sorted(Comparator.comparing(Consulta::getDataHora))
                .toList();
    }

    @Transactional(readOnly = true)
    public Consulta buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Consulta", id));
    }

    public Consulta agendar(ConsultaRequest req) {
        exigir(req.tipo() != null, "Informe o tipo da consulta: ROTINA ou ESPECIALIZADA.");
        exigir(req.pacienteId() != null && req.profissionalId() != null, "Informe pacienteId e profissionalId.");
        Paciente paciente = pacienteService.buscar(req.pacienteId());
        ProfissionalSaude profissional = profissionalService.buscar(req.profissionalId());

        Consulta consulta = switch (req.tipo()) {
            case ROTINA -> new ConsultaRotina(paciente, profissional, req.dataHora(), req.motivo());
            case ESPECIALIZADA -> criarEspecializada(req, paciente, profissional);
        };
        return salvarNovaConsulta(consulta);
    }

    private ConsultaEspecializada criarEspecializada(ConsultaRequest req, Paciente paciente,
                                                     ProfissionalSaude profissional) {
        ConsultaEspecializada consulta;
        if (req.encaminhadoPorId() != null) {
            consulta = profissionalService.buscarClinico(req.encaminhadoPorId())
                    .encaminhar(paciente, profissional, req.especialidade(), req.dataHora(), req.motivo());
        } else {
            consulta = new ConsultaEspecializada(paciente, profissional, req.dataHora(), req.motivo(),
                    req.especialidade());
        }
        return consulta;
    }

    /** Muda a situação respeitando as transições permitidas pelo enum SituacaoConsulta. */
    public Consulta alterarSituacao(Long id, MudancaSituacaoRequest req) {
        Consulta consulta = buscar(id);
        exigir(req.situacao() != null, "Informe a nova situação.");
        if (req.situacao() == SituacaoConsulta.CONCLUIDA) {
            consulta.concluir(req.observacoes());
        } else {
            consulta.alterarSituacao(req.situacao());
        }
        return consulta;
    }

    public Consulta registrarObservacao(Long id, ObservacaoRequest req) {
        Consulta consulta = buscar(id);
        exigir(req.profissionalId() != null, "Informe o profissionalId de quem está registrando a observação.");
        profissionalService.buscar(req.profissionalId()).registrarObservacao(consulta, req.texto());
        return consulta;
    }

    /** Gera uma NOVA consulta a partir de uma cancelada / com falta. A original continua como está. */
    public Consulta reagendar(Long id, LocalDateTime novaDataHora) {
        exigir(novaDataHora != null, "Informe a nova data e horário.");
        Consulta nova = buscar(id).reagendar(novaDataHora);
        return salvarNovaConsulta(nova);
    }

    private Consulta salvarNovaConsulta(Consulta consulta) {
        consulta.validar();
        exigir(consulta.getProfissional().estaDisponivel(consulta.getDataHora(), consulta.getDataHoraFimPrevista()),
                "O profissional " + consulta.getProfissional().getNome() + " já tem um atendimento nesse horário.");
        exigir(!consulta.getPaciente().possuiAtendimentoNoHorario(consulta.getDataHora(),
                        consulta.getDataHoraFimPrevista()),
                "O paciente já tem um atendimento nesse horário.");
        consulta.getPaciente().adicionarAtendimento(consulta);
        consulta.getProfissional().adicionarAtendimento(consulta);
        return repository.save(consulta);
    }
}
