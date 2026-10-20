package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    private ProfissionalSaude profissional;

    @ManyToOne
    @JoinColumn(name = "quarto_id")
    private Quarto quarto;

    protected Internacao() {
    }

    public Internacao(Paciente paciente, ProfissionalSaude profissional, Quarto quarto,
                      LocalDate dataPrevistaAlta, String observacoes) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.quarto = quarto;
        this.dataPrevistaAlta = dataPrevistaAlta;
        this.observacoes = observacoes;
    }

    /** Valida as regras, ocupa um leito do quarto e deixa a internação ATIVA. */
    public void iniciar() {
        exigir(status == null, "Esta internação já foi iniciada.");
        exigir(paciente != null, "A internação precisa de um paciente.");
        exigir(profissional != null, "A internação precisa de um profissional responsável.");
        exigir(quarto != null, "A internação precisa de um quarto.");
        exigir(!paciente.possuiInternacaoAtiva(), "O paciente " + paciente.getNome() + " já possui uma internação ativa.");
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
        quarto.liberar();
        this.dataEfetivaAlta = data;
        this.status = StatusInternacao.ENCERRADA;
    }

    public void cancelar() {
        exigir(estaAtiva(), "Só é possível cancelar uma internação ativa.");
        quarto.liberar();
        this.status = StatusInternacao.CANCELADA;
    }

    /** Move o paciente para outro quarto: ocupa o novo antes de liberar o antigo. */
    public void transferir(Quarto novoQuarto) {
        exigir(estaAtiva(), "Só é possível transferir uma internação ativa.");
        exigir(novoQuarto != null, "Informe o quarto de destino.");
        exigir(novoQuarto != quarto && !mesmoId(novoQuarto, quarto), "O paciente já está neste quarto.");
        exigirJustificativaSeNecessario(novoQuarto);
        novoQuarto.ocupar();
        quarto.liberar();
        this.quarto = novoQuarto;
    }

    public boolean estaAtiva() {
        return status == StatusInternacao.ATIVA;
    }

    /** Dias internado até a alta (ou até hoje, se ainda estiver internado). */
    @JsonProperty("diasInternado")
    public long calcularDiasInternado() {
        LocalDate fim = dataEfetivaAlta != null ? dataEfetivaAlta : LocalDate.now();
        return dataEntrada == null ? 0 : ChronoUnit.DAYS.between(dataEntrada.toLocalDate(), fim);
    }

    public void atualizarObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    /** Regra dos tipos de quarto: UTI e isolamento exigem justificativa clínica. */
    private void exigirJustificativaSeNecessario(Quarto destino) {
        exigir(!destino.getTipo().exigeJustificativa() || !vazio(observacoes),
                "Internações em " + destino.getTipo() + " exigem uma justificativa clínica nas observações.");
    }

    private static boolean mesmoId(Quarto a, Quarto b) {
        return a.getId() != null && a.getId().equals(b.getId());
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

    public ProfissionalSaude getProfissional() {
        return profissional;
    }

    public Quarto getQuarto() {
        return quarto;
    }
}
