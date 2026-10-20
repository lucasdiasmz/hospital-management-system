package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import java.time.LocalDateTime;

import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Atendimento de uma especialidade. Regra: o médico precisa atuar
 * na mesma especialidade da consulta.
 */
@Entity
@DiscriminatorValue("CONSULTA_ESPECIALIZADA")
public class ConsultaEspecializada extends Consulta {

    public static final int DURACAO_MINUTOS = 45;

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    /** Clínico geral que fez o encaminhamento (opcional). */
    @ManyToOne
    @JoinColumn(name = "encaminhado_por_id")
    private ClinicoGeral encaminhadoPor;

    protected ConsultaEspecializada() {
    }

    public ConsultaEspecializada(Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora,
                                 String motivo, Especialidade especialidade) {
        super(paciente, profissional, dataHora, motivo);
        this.especialidade = especialidade;
    }

    @Override
    protected void validarRegrasDaModalidade() {
        exigir(especialidade != null, "Informe a especialidade da consulta.");
        exigirEspecialidade(especialidade);
    }

    void definirEncaminhamento(ClinicoGeral clinico) {
        this.encaminhadoPor = clinico;
    }

    @Override
    protected Consulta criarCopia(LocalDateTime novaDataHora) {
        ConsultaEspecializada copia = new ConsultaEspecializada(getPaciente(), getProfissional(), novaDataHora,
                getMotivo(), especialidade);
        copia.encaminhadoPor = encaminhadoPor;
        return copia;
    }

    @Override
    public String getTipo() {
        return "CONSULTA_ESPECIALIZADA";
    }

    @Override
    public int getDuracaoEstimadaMinutos() {
        return DURACAO_MINUTOS;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public ClinicoGeral getEncaminhadoPor() {
        return encaminhadoPor;
    }
}
