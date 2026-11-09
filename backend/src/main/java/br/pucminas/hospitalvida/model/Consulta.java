package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * O que as consultas de rotina e especializada têm em comum: motivo e situação.
 * A situação só muda pelas transições permitidas em SituacaoConsulta.
 */
@Entity
public abstract class Consulta extends Atendimento {

    @Column(length = 1000)
    private String motivo;

    @Enumerated(EnumType.STRING)
    private SituacaoConsulta situacao;

    /** Preenchida quando esta consulta é o novo agendamento de uma cancelada ou com falta. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_original_id")
    private Consulta consultaOriginal;

    protected Consulta() {
    }

    protected Consulta(LocalDate data, LocalTime horario, Paciente paciente, Profissional profissional,
                       String motivo, String observacoes) {
        super(data, horario, paciente, profissional, observacoes);
        this.motivo = motivo;
        this.situacao = SituacaoConsulta.AGENDADA;
    }

    /** Cria uma consulta igual a esta em outra data (usado no reagendamento). */
    protected abstract Consulta criarCopia(LocalDate novaData, LocalTime novoHorario);

    protected void validarDadosDaConsulta() {
        validarDadosComuns();
        exigir(getProfissional() != null, "Informe o médico responsável pela consulta.");
        getProfissional().exigirAtivo();
        exigir(!vazio(motivo), "Informe o motivo da consulta.");
    }

    /** Além das condições de realização, só permite marcar para o futuro. */
    public void validarAgendamento() {
        validarRealizacao();
        exigir(LocalDateTime.of(getData(), getHorario()).isAfter(LocalDateTime.now()),
                "A consulta deve ser agendada para uma data e um horário futuros.");
    }

    public void alterarSituacao(SituacaoConsulta nova) {
        exigir(nova != null, "Informe a nova situação da consulta.");
        exigir(situacao.podeMudarPara(nova), "Não é possível mudar uma consulta de " + situacao + " para " + nova + ".");
        if (nova == SituacaoConsulta.EM_ATENDIMENTO) {
            validarRealizacao();
        }
        this.situacao = nova;
    }

    /** As observações são opcionais e seguem a regra de podeReceberObservacoes(). */
    public void alterarSituacao(SituacaoConsulta nova, String observacoes) {
        alterarSituacao(nova);
        if (!vazio(observacoes)) {
            registrarObservacoes(observacoes);
        }
    }

    /** Gera um novo agendamento ligado a esta consulta, que continua como estava. */
    public Consulta reagendar(LocalDate novaData, LocalTime novoHorario) {
        exigir(situacao == SituacaoConsulta.CANCELADA || situacao == SituacaoConsulta.NAO_COMPARECEU,
                "Só é possível reagendar consultas canceladas ou em que o paciente não compareceu.");
        Consulta nova = criarCopia(novaData, novoHorario);
        nova.consultaOriginal = this;
        nova.validarAgendamento();
        return nova;
    }

    /** Observações clínicas só depois que o atendimento começa. */
    @Override
    protected boolean podeReceberObservacoes() {
        return situacao == SituacaoConsulta.EM_ATENDIMENTO || situacao == SituacaoConsulta.CONCLUIDA;
    }

    public String getMotivo() {
        return motivo;
    }

    public SituacaoConsulta getSituacao() {
        return situacao;
    }

    public Long getConsultaOriginalId() {
        return consultaOriginal == null ? null : consultaOriginal.getId();
    }
}
