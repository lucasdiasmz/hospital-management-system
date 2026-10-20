package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDateTime;

import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Atendimento imediato. O paciente chega, recebe uma prioridade e fica
 * AGUARDANDO_AVALIACAO até um médico assumir o caso.
 * Por isso o profissional pode ser nulo no início.
 */
@Entity
@DiscriminatorValue("EMERGENCIA")
public class AtendimentoEmergencia extends Atendimento {

    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_emergencia")
    private StatusEmergencia status;

    private String queixaPrincipal;

    protected AtendimentoEmergencia() {
    }

    /** A data/hora do atendimento é o momento da chegada do paciente. */
    public AtendimentoEmergencia(Paciente paciente, Prioridade prioridade, String queixaPrincipal) {
        super(paciente, null, LocalDateTime.now());
        this.prioridade = prioridade;
        this.queixaPrincipal = queixaPrincipal;
        this.status = StatusEmergencia.AGUARDANDO_AVALIACAO;
    }

    @Override
    protected void validarCondicoes() {
        exigir(prioridade != null, "A classificação de prioridade é obrigatória (BAIXA, MODERADA, ALTA ou CRITICA).");
        exigir(!vazio(queixaPrincipal), "Informe a queixa principal do paciente.");
    }

    /** Um médico assume o caso e começa a avaliação. */
    public void iniciarAvaliacao(ProfissionalSaude medico) {
        exigir(medico != null, "Informe o médico que vai realizar a avaliação.");
        mudarStatus(StatusEmergencia.EM_ATENDIMENTO);
        this.profissional = medico;
    }

    public void concluir(String observacoes) {
        mudarStatus(StatusEmergencia.CONCLUIDO);
        if (!vazio(observacoes)) {
            registrarObservacoes(observacoes);
        }
    }

    private void mudarStatus(StatusEmergencia novo) {
        exigir(status.podeMudarPara(novo),
                "Não é possível mudar o atendimento de emergência de " + status + " para " + novo + ".");
        this.status = novo;
    }

    public boolean aguardandoAvaliacao() {
        return status == StatusEmergencia.AGUARDANDO_AVALIACAO;
    }

    /** Emergência não é agendada, então não bloqueia horários na agenda. */
    @Override
    public boolean ocupaAgenda() {
        return false;
    }

    @Override
    protected boolean podeReceberObservacoes() {
        return status != StatusEmergencia.AGUARDANDO_AVALIACAO;
    }

    @Override
    public String getTipo() {
        return "EMERGENCIA";
    }

    @Override
    public int getDuracaoEstimadaMinutos() {
        return 0;
    }

    public Prioridade getPrioridade() {
        return prioridade;
    }

    public StatusEmergencia getStatus() {
        return status;
    }

    public String getQueixaPrincipal() {
        return queixaPrincipal;
    }
}
