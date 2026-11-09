package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import br.pucminas.hospitalvida.model.enums.StatusProcedimento;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Procedimento cirúrgico agendado ou realizado.
 * A data e o horário herdados de Atendimento são os previstos; os efetivos são
 * preenchidos quando o procedimento é iniciado e concluído.
 */
@Entity
@DiscriminatorValue("PROCEDIMENTO")
public class ProcedimentoCirurgico extends Atendimento {

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoCirurgia tipoCirurgia;

    @Column(length = 2000)
    private String observacoesPreOperatorias;

    @Column(length = 2000)
    private String observacoesPosOperatorias;

    private LocalDateTime inicioEfetivo;
    private LocalDateTime fimEfetivo;

    /** Coluna com nome próprio porque a tabela é compartilhada com as outras modalidades. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_procedimento")
    private StatusProcedimento status;

    protected ProcedimentoCirurgico() {
    }

    public ProcedimentoCirurgico(LocalDate dataPrevista, LocalTime horarioPrevisto, Paciente paciente,
                                 Profissional cirurgiao, String descricao, TipoCirurgia tipoCirurgia,
                                 String observacoesPreOperatorias) {
        super(dataPrevista, horarioPrevisto, paciente, cirurgiao, null);
        this.descricao = descricao;
        this.tipoCirurgia = tipoCirurgia;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        this.status = StatusProcedimento.AGENDADO;
    }

    @Override
    public void validarRealizacao() {
        validarDadosComuns();
        exigir(getProfissional() != null, "Informe o cirurgião responsável pelo procedimento.");
        exigir(getProfissional().podeRealizarCirurgia(),
                "O profissional " + getProfissional().getNome() + " não é cirurgião.");
        getProfissional().exigirAtivo();
        exigir(!vazio(descricao), "Informe a descrição do procedimento.");
        exigir(tipoCirurgia != null, "Informe o tipo de cirurgia: ELETIVA, URGENCIA ou EMERGENCIA.");
    }

    /** Além das condições de realização, uma cirurgia eletiva só pode ser marcada para o futuro. */
    public void validarAgendamento() {
        validarRealizacao();
        exigir(tipoCirurgia != TipoCirurgia.ELETIVA
                        || LocalDateTime.of(getData(), getHorario()).isAfter(LocalDateTime.now()),
                "Cirurgias eletivas devem ser agendadas para uma data e um horário futuros.");
    }

    public void iniciar() {
        mudarStatus(StatusProcedimento.EM_ANDAMENTO);
        validarRealizacao();
        this.inicioEfetivo = LocalDateTime.now();
    }

    public void concluir(String observacoesPosOperatorias) {
        exigir(status != StatusProcedimento.AGENDADO, "Não é possível concluir um procedimento que não foi iniciado.");
        mudarStatus(StatusProcedimento.CONCLUIDO);
        this.fimEfetivo = LocalDateTime.now();
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }

    public void cancelar() {
        mudarStatus(StatusProcedimento.CANCELADO);
    }

    private void mudarStatus(StatusProcedimento novo) {
        exigir(status.podeMudarPara(novo), "Não é possível mudar o procedimento de " + status + " para " + novo + ".");
        this.status = novo;
    }

    @Override
    protected boolean podeReceberObservacoes() {
        return status == StatusProcedimento.EM_ANDAMENTO || status == StatusProcedimento.CONCLUIDO;
    }

    @Override
    public String getTipo() {
        return "PROCEDIMENTO";
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoCirurgia getTipoCirurgia() {
        return tipoCirurgia;
    }

    public String getObservacoesPreOperatorias() {
        return observacoesPreOperatorias;
    }

    public String getObservacoesPosOperatorias() {
        return observacoesPosOperatorias;
    }

    public LocalDateTime getInicioEfetivo() {
        return inicioEfetivo;
    }

    public LocalDateTime getFimEfetivo() {
        return fimEfetivo;
    }

    public StatusProcedimento getStatus() {
        return status;
    }
}
