package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;

/**
 * Base de todos os profissionais. Cada categoria (ClinicoGeral, MedicoEspecialista,
 * Cirurgiao) é uma subclasse e responde de forma diferente aos métodos
 * realizaConsultaRotina(), atendeEspecialidade() e podeRealizarCirurgias().
 * Assim as modalidades de atendimento não precisam de "if (tipo == ...)" espalhados.
 *
 * No banco, todas as categorias ficam numa tabela só (SINGLE_TABLE), diferenciadas
 * pela coluna "categoria".
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "categoria")
public abstract class ProfissionalSaude extends Pessoa {

    @Column(unique = true)
    private String registroProfissional;

    @JsonIgnore
    @OneToMany(mappedBy = "profissional")
    private List<Atendimento> atendimentos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "profissional")
    private List<Internacao> internacoes = new ArrayList<>();

    protected ProfissionalSaude() {
    }

    protected ProfissionalSaude(String nome, String registroProfissional, String telefone, String email) {
        super(nome, telefone, email);
        exigir(!vazio(registroProfissional), "O registro profissional (CRM) é obrigatório.");
        this.registroProfissional = registroProfissional;
    }

    public abstract CategoriaProfissional getCategoria();

    /** Por padrão, só o clínico geral realiza consultas de rotina. */
    public boolean realizaConsultaRotina() {
        return false;
    }

    /** Por padrão, o profissional não atende especialidade nenhuma. */
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return false;
    }

    /** Por padrão, só o cirurgião realiza cirurgias. */
    public boolean podeRealizarCirurgias() {
        return false;
    }

    /** Verifica se o profissional não tem atendimento marcado entre inicio e fim. */
    public boolean estaDisponivel(LocalDateTime inicio, LocalDateTime fim) {
        boolean disponivel = true;
        for (Atendimento atendimento : atendimentos) {
            if (atendimento.ocupaAgenda() && atendimento.conflitaCom(inicio, fim)) {
                disponivel = false;
            }
        }
        return disponivel;
    }

    public boolean ehResponsavelPor(Atendimento atendimento) {
        ProfissionalSaude responsavel = atendimento.getProfissional();
        return responsavel == this
                || (responsavel != null && id != null && id.equals(responsavel.getId()));
    }

    /** Só o profissional responsável pode registrar observações no atendimento. */
    public void registrarObservacao(Atendimento atendimento, String texto) {
        exigir(ehResponsavelPor(atendimento),
                "Somente o profissional responsável pode registrar observações neste atendimento.");
        atendimento.registrarObservacoes(texto);
    }

    public void adicionarAtendimento(Atendimento atendimento) {
        atendimentos.add(atendimento);
    }

    public void adicionarInternacao(Internacao internacao) {
        internacoes.add(internacao);
    }

    public boolean possuiRegistros() {
        return !atendimentos.isEmpty() || !internacoes.isEmpty();
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public List<Atendimento> getAtendimentos() {
        return atendimentos;
    }

    public List<Internacao> getInternacoes() {
        return internacoes;
    }
}
