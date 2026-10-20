package br.pucminas.hospitalvida.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

/**
 * Consolida todos os atendimentos e internações de um paciente.
 * Em vez de guardar listas duplicadas (como no diagrama da Sprint 1),
 * ele lê as listas do próprio paciente, evitando dados inconsistentes.
 */
@Entity
public class HistoricoMedico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(mappedBy = "historicoMedico")
    private Paciente paciente;

    @Column(length = 2000)
    private String informacoesRelevantes;

    protected HistoricoMedico() {
    }

    public HistoricoMedico(Paciente paciente) {
        this.paciente = paciente;
    }

    /** Atendimentos do paciente, do mais recente para o mais antigo. */
    public List<Atendimento> listarAtendimentos() {
        List<Atendimento> lista = new ArrayList<>(paciente.getAtendimentos());
        lista.sort(Comparator.comparing(Atendimento::getDataHora).reversed());
        return lista;
    }

    /** Internações do paciente, da mais recente para a mais antiga. */
    public List<Internacao> listarInternacoes() {
        List<Internacao> lista = new ArrayList<>(paciente.getInternacoes());
        lista.sort(Comparator.comparing(Internacao::getDataEntrada).reversed());
        return lista;
    }

    public void atualizarInformacoes(String informacoesRelevantes) {
        this.informacoesRelevantes = informacoesRelevantes;
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public String getInformacoesRelevantes() {
        return informacoesRelevantes;
    }
}
