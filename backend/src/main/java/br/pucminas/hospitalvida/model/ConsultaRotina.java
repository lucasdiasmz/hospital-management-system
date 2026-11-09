package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Avaliação geral do paciente, feita por um médico clínico geral. */
@Entity
@DiscriminatorValue("ROTINA")
public class ConsultaRotina extends Consulta {

    protected ConsultaRotina() {
    }

    public ConsultaRotina(LocalDate data, LocalTime horario, Paciente paciente, Profissional profissional,
                          String motivo, String observacoes) {
        super(data, horario, paciente, profissional, motivo, observacoes);
    }

    @Override
    public void validarRealizacao() {
        validarDadosDaConsulta();
        exigir(getProfissional().podeRealizarConsultaRotina(),
                "A consulta de rotina deve ser realizada por um médico clínico geral.");
    }

    @Override
    protected Consulta criarCopia(LocalDate novaData, LocalTime novoHorario) {
        return new ConsultaRotina(novaData, novoHorario, getPaciente(), getProfissional(), getMotivo(),
                getObservacoes());
    }

    @Override
    public String getTipo() {
        return "ROTINA";
    }
}
