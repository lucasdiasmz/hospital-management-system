package br.pucminas.hospitalvida.model;

import java.time.LocalDateTime;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Registro de que um clínico geral encaminhou um paciente para uma especialidade.
 *
 * Fica separado da consulta de propósito: o encaminhamento existe antes de a consulta
 * especializada ser marcada. A Pessoa 3 pode ligar a ConsultaEspecializada a ele depois.
 * Só é criado por MedicoClinicoGeral.encaminhar(...).
 */
@Entity
public class Encaminhamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "clinico_id")
    private MedicoClinicoGeral clinico;

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    /** Opcional: o clínico pode indicar um especialista específico. */
    @ManyToOne
    @JoinColumn(name = "especialista_id")
    private Profissional especialistaIndicado;

    private String motivo;
    private LocalDateTime dataHora;

    protected Encaminhamento() {
    }

    Encaminhamento(Paciente paciente, MedicoClinicoGeral clinico, Especialidade especialidade,
                   Profissional especialistaIndicado, String motivo) {
        exigir(paciente != null, "Informe o paciente que será encaminhado.");
        exigir(especialidade != null, "Informe a especialidade do encaminhamento.");
        exigir(motivo != null && !motivo.isBlank(), "Informe o motivo do encaminhamento.");
        exigir(especialistaIndicado == null || especialistaIndicado.atendeEspecialidade(especialidade),
                "O profissional indicado não atende a especialidade " + especialidade + ".");
        this.paciente = paciente;
        this.clinico = clinico;
        this.especialidade = especialidade;
        this.especialistaIndicado = especialistaIndicado;
        this.motivo = motivo.trim();
        this.dataHora = LocalDateTime.now();
    }

    private static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public MedicoClinicoGeral getClinico() {
        return clinico;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public Profissional getEspecialistaIndicado() {
        return especialistaIndicado;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
