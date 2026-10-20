package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDateTime;

import br.pucminas.hospitalvida.model.enums.StatusProcedimento;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Procedimento cirúrgico agendado ou realizado.
 * A data/hora herdada de Atendimento é a data PREVISTA; as datas efetivas
 * são preenchidas quando o procedimento é iniciado e concluído.
 */
@Entity
@DiscriminatorValue("PROCEDIMENTO_CIRURGICO")
public class ProcedimentoCirurgico extends Atendimento {

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoCirurgia tipoCirurgia;

    @Column(length = 2000)
    private String observacoesPreOperatorias;

    @Column(length = 2000)
    private String observacoesPosOperatorias;

    private int duracaoPrevistaMinutos;
    private LocalDateTime dataHoraInicioEfetivo;
    private LocalDateTime dataHoraFimEfetivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_procedimento")
    private StatusProcedimento status;

    protected ProcedimentoCirurgico() {
    }

    public ProcedimentoCirurgico(Paciente paciente, ProfissionalSaude cirurgiao, String descricao,
                                 TipoCirurgia tipoCirurgia, LocalDateTime dataHoraPrevista,
                                 int duracaoPrevistaMinutos, String observacoesPreOperatorias) {
        super(paciente, cirurgiao, dataHoraPrevista);
        this.descricao = descricao;
        this.tipoCirurgia = tipoCirurgia;
        this.duracaoPrevistaMinutos = duracaoPrevistaMinutos;
        this.observacoesPreOperatorias = observacoesPreOperatorias;
        this.status = StatusProcedimento.AGENDADO;
    }

    @Override
    protected void validarCondicoes() {
        exigir(profissional != null, "O procedimento precisa de um cirurgião responsável.");
        exigir(profissional.podeRealizarCirurgias(), "O profissional " + profissional.getNome()
                + " não é cirurgião e não pode ser responsável por um procedimento cirúrgico.");
        exigir(!vazio(descricao), "A descrição do procedimento é obrigatória.");
        exigir(tipoCirurgia != null, "O tipo de cirurgia é obrigatório (ELETIVA, URGENCIA ou EMERGENCIA).");
        exigir(duracaoPrevistaMinutos > 0, "A duração prevista deve ser maior que zero.");
        exigir(tipoCirurgia != TipoCirurgia.ELETIVA || getDataHora().isAfter(LocalDateTime.now()),
                "Cirurgias eletivas devem ser agendadas para uma data futura.");
    }

    public void iniciar() {
        mudarStatus(StatusProcedimento.EM_ANDAMENTO);
        this.dataHoraInicioEfetivo = LocalDateTime.now();
    }

    public void concluir(String observacoesPosOperatorias) {
        exigir(status != StatusProcedimento.AGENDADO,
                "Não é possível concluir um procedimento que ainda não foi iniciado.");
        mudarStatus(StatusProcedimento.CONCLUIDO);
        this.dataHoraFimEfetivo = LocalDateTime.now();
        this.observacoesPosOperatorias = observacoesPosOperatorias;
    }

    public void cancelar() {
        mudarStatus(StatusProcedimento.CANCELADO);
    }

    public void registrarObservacoesPreOperatorias(String texto) {
        exigir(status == StatusProcedimento.AGENDADO,
                "Observações pré-operatórias só podem ser registradas antes do início do procedimento.");
        this.observacoesPreOperatorias = texto;
    }

    private void mudarStatus(StatusProcedimento novo) {
        exigir(status.podeMudarPara(novo),
                "Não é possível mudar o procedimento de " + status + " para " + novo + ".");
        this.status = novo;
    }

    @Override
    public boolean ocupaAgenda() {
        return status != StatusProcedimento.CANCELADO;
    }

    @Override
    protected boolean podeReceberObservacoes() {
        return status == StatusProcedimento.EM_ANDAMENTO || status == StatusProcedimento.CONCLUIDO;
    }

    @Override
    public String getTipo() {
        return "PROCEDIMENTO_CIRURGICO";
    }

    @Override
    public int getDuracaoEstimadaMinutos() {
        return duracaoPrevistaMinutos;
    }

    public LocalDateTime getDataHoraPrevista() {
        return getDataHora();
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

    public int getDuracaoPrevistaMinutos() {
        return duracaoPrevistaMinutos;
    }

    public LocalDateTime getDataHoraInicioEfetivo() {
        return dataHoraInicioEfetivo;
    }

    public LocalDateTime getDataHoraFimEfetivo() {
        return dataHoraFimEfetivo;
    }

    public StatusProcedimento getStatus() {
        return status;
    }
}
