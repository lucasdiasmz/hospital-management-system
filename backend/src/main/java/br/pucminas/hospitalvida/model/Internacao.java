package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Mantém a ocupação do quarto em dia ao iniciar, dar alta, cancelar ou transferir. */
@Entity
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataEntrada;
    private LocalDate dataPrevistaAlta;
    private LocalDate dataEfetivaAlta;

    @Column(length = 2000)
    private String observacoes;

    @Enumerated(EnumType.STRING)
    private StatusInternacao status;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @ManyToOne
    @JoinColumn(name = "quarto_id")
    private Quarto quarto;

    protected Internacao() {
        // exigido pelo JPA
    }

    public Internacao(Paciente paciente, Profissional profissional, Quarto quarto, LocalDate dataPrevistaAlta,
                      String observacoes) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.quarto = quarto;
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes;
    }

    /** Valida as regras, ocupa um leito do quarto e deixa a internação ATIVA. */
    public void iniciar() {
        exigir(status == null, "Esta internação já foi iniciada.");
        exigir(paciente != null, "Informe o paciente da internação.");
        exigir(profissional != null, "Informe o profissional responsável pela internação.");
        profissional.exigirAtivo();
        exigir(quarto != null, "Informe o quarto da internação.");
        exigir(dataPrevistaAlta == null || !dataPrevistaAlta.isBefore(LocalDate.now()),
                "A data prevista de alta não pode estar no passado.");
        exigirJustificativaSeNecessario(quarto);
        quarto.ocupar();
        this.dataEntrada = LocalDateTime.now();
        this.status = StatusInternacao.ATIVA;
    }

    public void registrarAlta(LocalDate data) {
        exigir(estaAtiva(), "Só é possível dar alta em uma internação ativa.");
        exigir(data != null, "Informe a data da alta.");
        exigir(!data.isBefore(dataEntrada.toLocalDate()), "A data de alta não pode ser anterior à data de entrada.");
        exigir(!data.isAfter(LocalDate.now()), "A data de alta não pode estar no futuro.");
        quarto.liberar();
        this.dataEfetivaAlta = data;
        this.status = StatusInternacao.ENCERRADA;
    }

    public void cancelar() {
        exigir(estaAtiva(), "Só é possível cancelar uma internação ativa.");
        quarto.liberar();
        this.status = StatusInternacao.CANCELADA;
    }

    /** Ocupa o novo quarto antes de liberar o antigo: se o destino estiver lotado, nada muda. */
    public void transferir(Quarto novoQuarto) {
        exigir(estaAtiva(), "Só é possível transferir uma internação ativa.");
        exigir(novoQuarto != null, "Informe o quarto de destino.");
        exigir(novoQuarto != quarto && (novoQuarto.getId() == null || !novoQuarto.getId().equals(quarto.getId())),
                "O paciente já está neste quarto.");
        exigirJustificativaSeNecessario(novoQuarto);
        novoQuarto.ocupar();
        quarto.liberar();
        this.quarto = novoQuarto;
    }

    public boolean estaAtiva() {
        return status == StatusInternacao.ATIVA;
    }

    private void exigirJustificativaSeNecessario(Quarto destino) {
        exigir(!destino.getTipo().exigeJustificativa() || (observacoes != null && !observacoes.isBlank()),
                "Internações em " + destino.getTipo() + " exigem uma justificativa clínica nas observações.");
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getDataEntrada() {
        return dataEntrada;
    }

    public LocalDate getDataPrevistaAlta() {
        return dataPrevistaAlta;
    }

    public LocalDate getDataEfetivaAlta() {
        return dataEfetivaAlta;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public StatusInternacao getStatus() {
        return status;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public Quarto getQuarto() {
        return quarto;
    }
}
