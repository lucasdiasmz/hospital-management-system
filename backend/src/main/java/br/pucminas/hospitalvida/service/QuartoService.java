package br.pucminas.hospitalvida.service;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.Requisicoes.QuartoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.model.Quarto;
import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;
import br.pucminas.hospitalvida.repository.InternacaoRepository;
import br.pucminas.hospitalvida.repository.QuartoRepository;

@Service
@Transactional
public class QuartoService {

    private final QuartoRepository repository;
    private final InternacaoRepository internacaoRepository;

    public QuartoService(QuartoRepository repository, InternacaoRepository internacaoRepository) {
        this.repository = repository;
        this.internacaoRepository = internacaoRepository;
    }

    /** Ex.: ?tipo=UTI&comVaga=true lista as UTIs com leito livre. */
    @Transactional(readOnly = true)
    public List<Quarto> listar(SituacaoQuarto situacao, TipoQuarto tipo, Boolean comVaga) {
        return repository.findAll().stream()
                .filter(q -> situacao == null || q.getSituacao() == situacao)
                .filter(q -> tipo == null || q.getTipo() == tipo)
                .filter(q -> comVaga == null || q.possuiVaga() == comVaga)
                .toList();
    }

    @Transactional(readOnly = true)
    public Quarto buscar(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Quarto", id));
    }

    public Quarto cadastrar(QuartoRequest req) {
        exigir(req.andar() != null && req.capacidadeMaxima() != null, "Informe andar e capacidadeMaxima.");
        Quarto quarto = new Quarto(req.numero(), req.andar(), req.tipo(), req.capacidadeMaxima());
        exigir(!repository.existsByNumero(quarto.getNumero()), "Já existe um quarto com o número " + quarto.getNumero() + ".");
        return repository.save(quarto);
    }

    public Quarto bloquear(Long id) {
        Quarto quarto = buscar(id);
        quarto.marcarIndisponivel();
        return quarto;
    }

    public Quarto desbloquear(Long id) {
        Quarto quarto = buscar(id);
        quarto.liberarParaUso();
        return quarto;
    }

    public void excluir(Long id) {
        Quarto quarto = buscar(id);
        exigir(!internacaoRepository.existsByQuarto_Id(id),
                "O quarto " + quarto.getNumero() + " possui internações registradas e não pode ser excluído.");
        repository.delete(quarto);
    }
}
