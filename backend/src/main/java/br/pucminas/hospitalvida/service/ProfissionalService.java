package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.Requisicoes.ProfissionalRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.Cirurgiao;
import br.pucminas.hospitalvida.model.ClinicoGeral;
import br.pucminas.hospitalvida.model.ConsultaEspecializada;
import br.pucminas.hospitalvida.model.MedicoEspecialista;
import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;
import br.pucminas.hospitalvida.model.ProfissionalSaude;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.repository.ProfissionalRepository;

@Service
@Transactional
public class ProfissionalService {

    private final ProfissionalRepository repository;

    public ProfissionalService(ProfissionalRepository repository) {
        this.repository = repository;
    }

    /**
     * Lista profissionais com filtros opcionais. Ex.: ?especialidade=CARDIOLOGIA&disponivelEm=2026-10-20T10:00
     * devolve os cardiologistas livres nesse horário.
     */
    @Transactional(readOnly = true)
    public List<ProfissionalSaude> listar(CategoriaProfissional categoria, Especialidade especialidade,
                                          LocalDateTime disponivelEm) {
        return repository.findAll().stream()
                .filter(p -> categoria == null || p.getCategoria() == categoria)
                .filter(p -> especialidade == null || p.atendeEspecialidade(especialidade))
                .filter(p -> disponivelEm == null
                        || p.estaDisponivel(disponivelEm, disponivelEm.plusMinutes(ConsultaEspecializada.DURACAO_MINUTOS)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfissionalSaude buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Profissional", id));
    }

    /** Cria a subclasse certa de acordo com a categoria informada. */
    public ProfissionalSaude cadastrar(ProfissionalRequest req) {
        exigir(req.categoria() != null, "Informe a categoria: CLINICO_GERAL, ESPECIALISTA ou CIRURGIAO.");
        ProfissionalSaude profissional = switch (req.categoria()) {
            case CLINICO_GERAL -> new ClinicoGeral(req.nome(), req.registroProfissional(), req.telefone(), req.email());
            case ESPECIALISTA -> new MedicoEspecialista(req.nome(), req.registroProfissional(), req.telefone(),
                    req.email(), req.especialidade());
            case CIRURGIAO -> new Cirurgiao(req.nome(), req.registroProfissional(), req.telefone(), req.email(),
                    req.areaCirurgica());
        };
        exigir(!repository.existsByRegistroProfissional(profissional.getRegistroProfissional()),
                "Já existe um profissional com o registro " + profissional.getRegistroProfissional() + ".");
        return repository.save(profissional);
    }

    /** Atualiza nome e contato (e a área cirúrgica, no caso de cirurgiões). */
    public ProfissionalSaude atualizar(Long id, ProfissionalRequest req) {
        ProfissionalSaude profissional = buscar(id);
        profissional.atualizarNome(req.nome());
        profissional.atualizarContato(req.telefone(), req.email());
        if (profissional instanceof Cirurgiao cirurgiao && req.areaCirurgica() != null) {
            cirurgiao.atualizarAreaCirurgica(req.areaCirurgica());
        }
        return profissional;
    }

    public void excluir(Long id) {
        ProfissionalSaude profissional = buscar(id);
        exigir(!profissional.possuiRegistros(),
                "O profissional possui atendimentos ou internações registrados e não pode ser excluído.");
        repository.delete(profissional);
    }

    @Transactional(readOnly = true)
    public List<ProcedimentoCirurgico> procedimentos(Long id) {
        return buscarCirurgiao(id).listarProcedimentos();
    }

    public Cirurgiao buscarCirurgiao(Long id) {
        ProfissionalSaude profissional = buscar(id);
        exigir(profissional.podeRealizarCirurgias(), "O profissional " + profissional.getNome() + " não é cirurgião.");
        return (Cirurgiao) profissional;
    }

    public ClinicoGeral buscarClinico(Long id) {
        ProfissionalSaude profissional = buscar(id);
        exigir(profissional.realizaConsultaRotina(),
                "Somente médicos clínicos gerais podem fazer encaminhamentos.");
        return (ClinicoGeral) profissional;
    }
}
