package br.pucminas.hospitalvida.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.pucminas.hospitalvida.dto.EncaminhamentoRequest;
import br.pucminas.hospitalvida.dto.ProfissionalRequest;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.Cirurgiao;
import br.pucminas.hospitalvida.model.Encaminhamento;
import br.pucminas.hospitalvida.model.MedicoClinicoGeral;
import br.pucminas.hospitalvida.model.MedicoEspecialista;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.Profissional;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.repository.EncaminhamentoRepository;
import br.pucminas.hospitalvida.repository.PacienteRepository;
import br.pucminas.hospitalvida.repository.ProfissionalRepository;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalRepository repository;
    private final EncaminhamentoRepository encaminhamentoRepository;
    private final PacienteRepository pacienteRepository;

    public ProfissionalService(ProfissionalRepository repository, EncaminhamentoRepository encaminhamentoRepository,
                               PacienteRepository pacienteRepository) {
        this.repository = repository;
        this.encaminhamentoRepository = encaminhamentoRepository;
        this.pacienteRepository = pacienteRepository;
    }

    /**
     * Lista os profissionais com filtros opcionais.
     * Por padrão mostra só os ativos (os "disponíveis"); incluirInativos=true mostra todos.
     * Ex.: GET /profissionais?especialidade=CARDIOLOGIA
     */
    @Transactional(readOnly = true)
    public List<Profissional> listar(CategoriaProfissional categoria, Especialidade especialidade,
                                     boolean incluirInativos) {
        return repository.findAll().stream()
                .filter(p -> incluirInativos || p.isAtivo())
                .filter(p -> categoria == null || p.getCategoria() == categoria)
                .filter(p -> especialidade == null || p.atendeEspecialidade(especialidade))
                .toList();
    }

    @Transactional(readOnly = true)
    public Profissional buscar(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profissional com id " + id + " não encontrado."));
    }

    /** Cria a subclasse certa de acordo com a categoria informada. */
    public Profissional cadastrar(ProfissionalRequest req) {
        exigir(req.categoria() != null, "Informe a categoria: CLINICO_GERAL, ESPECIALISTA ou CIRURGIAO.");
        Profissional profissional = switch (req.categoria()) {
            case CLINICO_GERAL -> new MedicoClinicoGeral(req.nome(), req.registroProfissional(),
                    req.telefone(), req.email());
            case ESPECIALISTA -> new MedicoEspecialista(req.nome(), req.registroProfissional(),
                    req.telefone(), req.email(), req.especialidade());
            case CIRURGIAO -> new Cirurgiao(req.nome(), req.registroProfissional(),
                    req.telefone(), req.email(), req.areaCirurgica(), req.rqe());
        };
        exigir(!repository.existsByRegistroProfissional(profissional.getRegistroProfissional()),
                "Já existe um profissional com o registro " + profissional.getRegistroProfissional() + ".");
        return repository.save(profissional);
    }

    /**
     * Atualiza nome e contato (e a atuação cirúrgica, se for cirurgião).
     * Categoria, especialidade e registro não mudam: para isso, cadastra-se outro profissional.
     */
    public Profissional atualizar(Long id, ProfissionalRequest req) {
        Profissional profissional = buscar(id);
        exigir(req.categoria() == null || req.categoria() == profissional.getCategoria(),
                "Não é possível mudar a categoria de um profissional já cadastrado.");
        profissional.atualizarNome(req.nome());
        profissional.atualizarContato(req.telefone(), req.email());
        if (profissional instanceof Cirurgiao cirurgiao) {
            cirurgiao.atualizarAtuacaoCirurgica(req.areaCirurgica(), req.rqe());
        }
        return profissional;
    }

    /** Não apaga do banco: o histórico de atendimentos continua apontando para ele. */
    public Profissional desativar(Long id) {
        Profissional profissional = buscar(id);
        profissional.desativar();
        return profissional;
    }

    public Profissional reativar(Long id) {
        Profissional profissional = buscar(id);
        profissional.reativar();
        return profissional;
    }

    // ---------- Encaminhamentos ----------

    public Encaminhamento encaminhar(Long clinicoId, EncaminhamentoRequest req) {
        Profissional profissional = buscar(clinicoId);
        exigir(profissional instanceof MedicoClinicoGeral,
                "Somente médicos clínicos gerais podem encaminhar pacientes.");
        exigir(req.pacienteId() != null, "Informe o pacienteId.");
        Paciente paciente = pacienteRepository.findById(req.pacienteId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Paciente com id " + req.pacienteId() + " não encontrado."));
        Profissional especialista = req.especialistaId() == null ? null : buscar(req.especialistaId());

        Encaminhamento encaminhamento = ((MedicoClinicoGeral) profissional)
                .encaminhar(paciente, req.especialidade(), especialista, req.motivo());
        return encaminhamentoRepository.save(encaminhamento);
    }

    @Transactional(readOnly = true)
    public List<Encaminhamento> encaminhamentosDoClinico(Long clinicoId) {
        buscar(clinicoId);
        return encaminhamentoRepository.findByClinico_Id(clinicoId);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
