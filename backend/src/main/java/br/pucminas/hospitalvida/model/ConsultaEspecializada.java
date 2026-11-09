package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/** Atendimento de uma especialidade: o médico precisa atuar nessa mesma área. */
@Entity
@DiscriminatorValue("ESPECIALIZADA")
public class ConsultaEspecializada extends Consulta {

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    /** Opcional: encaminhamento do clínico geral que originou esta consulta. */
    @ManyToOne
    @JoinColumn(name = "encaminhamento_id")
    private Encaminhamento encaminhamento;

    protected ConsultaEspecializada() {
    }

    public ConsultaEspecializada(LocalDate data, LocalTime horario, Paciente paciente, Profissional profissional,
                                 String motivo, String observacoes, Especialidade especialidade,
                                 Encaminhamento encaminhamento) {
        super(data, horario, paciente, profissional, motivo, observacoes);
        this.especialidade = especialidade;
        this.encaminhamento = encaminhamento;
    }

    @Override
    public void validarRealizacao() {
        validarDadosDaConsulta();
        exigir(especialidade != null, "Informe a especialidade da consulta.");
        exigir(getProfissional().atendeEspecialidade(especialidade),
                "O profissional " + getProfissional().getNome() + " não atende a especialidade " + especialidade + ".");
        if (encaminhamento != null) {
            exigir(Objects.equals(encaminhamento.getPaciente().getId(), getPaciente().getId()),
                    "O encaminhamento informado é de outro paciente.");
            exigir(encaminhamento.getEspecialidade() == especialidade,
                    "O encaminhamento informado é para a especialidade " + encaminhamento.getEspecialidade() + ".");
        }
    }

    @Override
    protected Consulta criarCopia(LocalDate novaData, LocalTime novoHorario) {
        return new ConsultaEspecializada(novaData, novoHorario, getPaciente(), getProfissional(), getMotivo(),
                getObservacoes(), especialidade, encaminhamento);
    }

    @Override
    public String getTipo() {
        return "ESPECIALIZADA";
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public Long getEncaminhamentoId() {
        return encaminhamento == null ? null : encaminhamento.getId();
    }
}
