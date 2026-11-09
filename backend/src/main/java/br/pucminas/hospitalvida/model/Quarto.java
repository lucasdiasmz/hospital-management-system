package br.pucminas.hospitalvida.model;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** A situação é sempre calculada a partir da ocupação, exceto quando o quarto está bloqueado. */
@Entity
public class Quarto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numero;

    private int andar;
    private int capacidadeMaxima;
    private int ocupacaoAtual;

    @Enumerated(EnumType.STRING)
    private TipoQuarto tipo;

    @Enumerated(EnumType.STRING)
    private SituacaoQuarto situacao;

    protected Quarto() {
        // exigido pelo JPA
    }

    public Quarto(String numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        exigir(numero != null && !numero.isBlank(), "O número do quarto é obrigatório.");
        exigir(tipo != null, "Informe o tipo do quarto: ENFERMARIA, APARTAMENTO, UTI ou ISOLAMENTO.");
        exigir(capacidadeMaxima >= 1, "O quarto precisa ter pelo menos 1 leito.");
        exigir(capacidadeMaxima <= tipo.getLimiteLeitos(),
                "Quartos do tipo " + tipo + " podem ter no máximo " + tipo.getLimiteLeitos() + " leito(s).");
        this.numero = numero.trim();
        this.andar = andar;
        this.tipo = tipo;
        this.capacidadeMaxima = capacidadeMaxima;
        this.situacao = SituacaoQuarto.DISPONIVEL;
    }

    public boolean possuiVaga() {
        return situacao != SituacaoQuarto.INDISPONIVEL && ocupacaoAtual < capacidadeMaxima;
    }

    public void ocupar() {
        exigir(situacao != SituacaoQuarto.INDISPONIVEL, "O quarto " + numero + " está indisponível.");
        exigir(ocupacaoAtual < capacidadeMaxima, "O quarto " + numero + " está lotado.");
        ocupacaoAtual++;
        atualizarSituacao();
    }

    public void liberar() {
        exigir(ocupacaoAtual > 0, "O quarto " + numero + " já está vazio.");
        ocupacaoAtual--;
        atualizarSituacao();
    }

    /** Bloqueia o quarto (ex.: manutenção). Só é permitido com o quarto vazio. */
    public void bloquear() {
        exigir(situacao != SituacaoQuarto.INDISPONIVEL, "O quarto " + numero + " já está bloqueado.");
        exigir(ocupacaoAtual == 0, "Só é possível bloquear o quarto " + numero + " quando ele estiver vazio.");
        situacao = SituacaoQuarto.INDISPONIVEL;
    }

    public void desbloquear() {
        exigir(situacao == SituacaoQuarto.INDISPONIVEL, "O quarto " + numero + " não está bloqueado.");
        situacao = SituacaoQuarto.DISPONIVEL;
    }

    private void atualizarSituacao() {
        if (ocupacaoAtual == 0) {
            situacao = SituacaoQuarto.DISPONIVEL;
        } else if (ocupacaoAtual == capacidadeMaxima) {
            situacao = SituacaoQuarto.LOTADO;
        } else {
            situacao = SituacaoQuarto.PARCIALMENTE_OCUPADO;
        }
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public int getAndar() {
        return andar;
    }

    public int getCapacidadeMaxima() {
        return capacidadeMaxima;
    }

    public int getOcupacaoAtual() {
        return ocupacaoAtual;
    }

    public TipoQuarto getTipo() {
        return tipo;
    }

    public SituacaoQuarto getSituacao() {
        return situacao;
    }
}
