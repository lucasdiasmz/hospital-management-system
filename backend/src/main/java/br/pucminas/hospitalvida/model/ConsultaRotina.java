package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.time.LocalDateTime;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Avaliação geral do paciente, feita por um clínico geral. */
@Entity
@DiscriminatorValue("CONSULTA_ROTINA")
public class ConsultaRotina extends Consulta {

    public static final int DURACAO_MINUTOS = 30;

    protected ConsultaRotina() {
    }

    public ConsultaRotina(Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora, String motivo) {
        super(paciente, profissional, dataHora, motivo);
    }

    @Override
    protected void validarRegrasDaModalidade() {
        exigir(profissional.realizaConsultaRotina(),
                "Consulta de rotina deve ser realizada por um médico clínico geral.");
    }

    @Override
    protected Consulta criarCopia(LocalDateTime novaDataHora) {
        return new ConsultaRotina(getPaciente(), getProfissional(), novaDataHora, getMotivo());
    }

    @Override
    public String getTipo() {
        return "CONSULTA_ROTINA";
    }

    @Override
    public int getDuracaoEstimadaMinutos() {
        return DURACAO_MINUTOS;
    }
}
