package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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
    private SituacaoQuarto situacao;

    @Enumerated(EnumType.STRING)
    private TipoQuarto tipo;

    protected Quarto() {
    }

    public Quarto(String numero, int andar, TipoQuarto tipo, int capacidadeMaxima) {
        exigir(!vazio(numero), "O número do quarto é obrigatório.");
        exigir(tipo != null, "O tipo do quarto é obrigatório (ENFERMARIA, APARTAMENTO, UTI ou ISOLAMENTO).");
        exigir(capacidadeMaxima >= 1, "A capacidade do quarto deve ser de pelo menos 1 leito.");
        exigir(capacidadeMaxima <= tipo.getLimiteLeitos(),
                "Quartos do tipo " + tipo + " podem ter no máximo " + tipo.getLimiteLeitos() + " leito(s).");
        this.numero = numero;
        this.andar = andar;
        this.tipo = tipo;
        this.capacidadeMaxima = capacidadeMaxima;
        this.ocupacaoAtual = 0;
        this.situacao = SituacaoQuarto.DISPONIVEL;
    }

    public boolean possuiVaga() {
        return situacao != SituacaoQuarto.INDISPONIVEL && ocupacaoAtual < capacidadeMaxima;
    }

    /** Ocupa um leito (usado ao iniciar ou transferir uma internação). */
    public void ocupar() {
        exigir(situacao != SituacaoQuarto.INDISPONIVEL, "O quarto " + numero + " está indisponível.");
        exigir(ocupacaoAtual < capacidadeMaxima, "O quarto " + numero + " está lotado.");
        ocupacaoAtual++;
        atualizarSituacao();
    }

    /** Libera um leito (usado na alta, cancelamento ou transferência). */
    public void liberar() {
        exigir(ocupacaoAtual > 0, "O quarto " + numero + " já está vazio.");
        ocupacaoAtual--;
        atualizarSituacao();
    }

    /** A situação é sempre calculada a partir da ocupação (exceto se estiver bloqueado). */
    public void atualizarSituacao() {
        if (situacao != SituacaoQuarto.INDISPONIVEL) {
            if (ocupacaoAtual == 0) {
                situacao = SituacaoQuarto.DISPONIVEL;
            } else if (ocupacaoAtual >= capacidadeMaxima) {
                situacao = SituacaoQuarto.LOTADO;
            } else {
                situacao = SituacaoQuarto.PARCIALMENTE_OCUPADO;
            }
        }
    }

    /** Bloqueia o quarto (ex.: manutenção). Só é permitido com o quarto vazio. */
    public void marcarIndisponivel() {
        exigir(ocupacaoAtual == 0, "Só é possível bloquear o quarto " + numero + " quando ele estiver vazio.");
        situacao = SituacaoQuarto.INDISPONIVEL;
    }

    public void liberarParaUso() {
        exigir(situacao == SituacaoQuarto.INDISPONIVEL, "O quarto " + numero + " não está bloqueado.");
        situacao = SituacaoQuarto.DISPONIVEL;
        atualizarSituacao();
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

    public SituacaoQuarto getSituacao() {
        return situacao;
    }

    public TipoQuarto getTipo() {
        return tipo;
    }
}
