package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * O que é comum às consultas (rotina e especializada): motivo e situação.
 * A situação é um enum e só muda pelas transições permitidas em SituacaoConsulta.
 */
@Entity
public abstract class Consulta extends Atendimento {

    public static final LocalTime ABERTURA = LocalTime.of(7, 0);
    public static final LocalTime FECHAMENTO = LocalTime.of(19, 0);

    private String motivo;

    @Enumerated(EnumType.STRING)
    private SituacaoConsulta situacao;

    /** Preenchido quando esta consulta é um reagendamento de outra. */
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "consulta_original_id")
    private Consulta consultaOriginal;

    protected Consulta() {
    }

    protected Consulta(Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora, String motivo) {
        super(paciente, profissional, dataHora);
        this.motivo = motivo;
        this.situacao = SituacaoConsulta.AGENDADA;
    }

    @Override
    protected void validarCondicoes() {
        exigir(profissional != null, "A consulta precisa de um médico responsável.");
        exigir(!vazio(motivo), "O motivo da consulta é obrigatório.");
        exigir(getDataHora().isAfter(LocalDateTime.now()), "A consulta deve ser agendada para uma data futura.");
        exigir(validarHorario(),
                "Consultas só podem ser marcadas de segunda a sábado, entre " + ABERTURA + " e " + FECHAMENTO + ".");
        validarRegrasDaModalidade();
    }

    /** Cada tipo de consulta verifica o profissional de um jeito. */
    protected abstract void validarRegrasDaModalidade();

    /** Cria uma nova consulta igual a esta, mas em outra data (usado no reagendamento). */
    protected abstract Consulta criarCopia(LocalDateTime novaDataHora);

    /** Horário de funcionamento: seg-sáb, e a consulta precisa terminar até o fechamento. */
    public boolean validarHorario() {
        LocalDateTime inicio = getDataHora();
        LocalTime fim = getDataHoraFimPrevista().toLocalTime();
        return inicio.getDayOfWeek() != DayOfWeek.SUNDAY
                && !inicio.toLocalTime().isBefore(ABERTURA)
                && !fim.isAfter(FECHAMENTO)
                && getDataHoraFimPrevista().toLocalDate().equals(inicio.toLocalDate());
    }

    // ---------- Mudanças de situação ----------

    public void alterarSituacao(SituacaoConsulta nova) {
        exigir(nova != null, "Informe a nova situação da consulta.");
        exigir(situacao.podeMudarPara(nova),
                "Não é possível mudar uma consulta de " + situacao + " para " + nova + ".");
        this.situacao = nova;
    }

    public void confirmar() {
        alterarSituacao(SituacaoConsulta.CONFIRMADA);
    }

    public void iniciarAtendimento() {
        alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO);
    }

    public void concluir(String observacoes) {
        alterarSituacao(SituacaoConsulta.CONCLUIDA);
        if (!vazio(observacoes)) {
            registrarObservacoes(observacoes);
        }
    }

    public void cancelar() {
        alterarSituacao(SituacaoConsulta.CANCELADA);
    }

    public void registrarFalta() {
        alterarSituacao(SituacaoConsulta.NAO_COMPARECEU);
    }

    /**
     * Uma consulta cancelada (ou em que o paciente faltou) não pode ser concluída.
     * Para atender o paciente, cria-se um novo agendamento ligado à consulta original.
     */
    public Consulta reagendar(LocalDateTime novaDataHora) {
        exigir(situacao == SituacaoConsulta.CANCELADA || situacao == SituacaoConsulta.NAO_COMPARECEU,
                "Só é possível reagendar consultas canceladas ou em que o paciente não compareceu.");
        Consulta nova = criarCopia(novaDataHora);
        nova.consultaOriginal = this;
        nova.validar();
        return nova;
    }

    @Override
    public boolean ocupaAgenda() {
        return situacao.ocupaAgenda();
    }

    /** Observações só depois que o atendimento começou. */
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

    public Consulta getConsultaOriginal() {
        return consultaOriginal;
    }

    public Long getConsultaOriginalId() {
        return consultaOriginal == null ? null : consultaOriginal.getId();
    }
}
