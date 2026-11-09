package br.pucminas.hospitalvida.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.QuartoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
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

    /** Filtros opcionais. Ex.: ?tipo=UTI&comVaga=true lista as UTIs com leito livre. */
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
        exigir(req.andar() != null, "Informe o andar do quarto.");
        exigir(req.capacidadeMaxima() != null, "Informe a capacidadeMaxima do quarto.");
        Quarto quarto = new Quarto(req.numero(), req.andar(), req.tipo(), req.capacidadeMaxima());
        exigir(!repository.existsByNumero(quarto.getNumero()), "Já existe um quarto com o número " + quarto.getNumero() + ".");
        return repository.save(quarto);
    }

    public Quarto bloquear(Long id) {
        Quarto quarto = buscar(id);
        quarto.bloquear();
        return quarto;
    }

    public Quarto desbloquear(Long id) {
        Quarto quarto = buscar(id);
        quarto.desbloquear();
        return quarto;
    }

    /** Quarto com internações registradas não é apagado, para manter o histórico. */
    public void excluir(Long id) {
        Quarto quarto = buscar(id);
        exigir(!internacaoRepository.existsByQuarto_Id(id),
                "O quarto " + quarto.getNumero() + " possui internações registradas e não pode ser excluído.");
        repository.delete(quarto);
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }
}
