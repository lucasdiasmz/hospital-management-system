package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Atendimento imediato: o paciente chega, recebe uma prioridade e fica AGUARDANDO até um
 * médico assumir o caso. Por isso o profissional fica vazio no início.
 */
@Entity
@DiscriminatorValue("EMERGENCIA")
public class AtendimentoEmergencia extends Atendimento {

    @Enumerated(EnumType.STRING)
    private Prioridade prioridade;

    /** Coluna com nome próprio porque a tabela é compartilhada com as outras modalidades. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_emergencia")
    private StatusEmergencia status;

    private String queixaPrincipal;

    protected AtendimentoEmergencia() {
    }

    /** A data e o horário são os da chegada do paciente. */
    public AtendimentoEmergencia(Paciente paciente, Prioridade prioridade, String queixaPrincipal) {
        super(LocalDate.now(), LocalTime.now().withNano(0), paciente, null, null);
        this.prioridade = prioridade;
        this.queixaPrincipal = queixaPrincipal;
        this.status = StatusEmergencia.AGUARDANDO;
    }

    @Override
    public void validarRealizacao() {
        validarDadosComuns();
        exigir(prioridade != null, "Informe a prioridade: BAIXA, MODERADA, ALTA ou CRITICA.");
        exigir(!vazio(queixaPrincipal), "Informe a queixa principal do paciente.");
        if (status != StatusEmergencia.AGUARDANDO) {
            exigir(getProfissional() != null, "Informe o médico responsável pelo atendimento.");
            getProfissional().exigirAtivo();
        }
    }

    /** Um médico assume o caso e começa a avaliação. */
    public void iniciar(Profissional medico) {
        exigir(medico != null, "Informe o médico que vai realizar a avaliação.");
        mudarStatus(StatusEmergencia.INICIADO);
        definirProfissional(medico);
        validarRealizacao();
    }

    public void concluir(String observacoes) {
        mudarStatus(StatusEmergencia.CONCLUIDO);
        if (!vazio(observacoes)) {
            registrarObservacoes(observacoes);
        }
    }

    private void mudarStatus(StatusEmergencia novo) {
        exigir(status.podeMudarPara(novo), "Não é possível mudar o atendimento de " + status + " para " + novo + ".");
        this.status = novo;
    }

    @Override
    protected boolean podeReceberObservacoes() {
        return status != StatusEmergencia.AGUARDANDO;
    }

    @Override
    public String getTipo() {
        return "EMERGENCIA";
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
