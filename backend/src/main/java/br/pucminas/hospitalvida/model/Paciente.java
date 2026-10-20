package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Paciente extends Pessoa {

    @Column(unique = true)
    private String cpf;

    private LocalDate dataNascimento;
    private String endereco;

    /** Composição: o histórico nasce e morre junto com o paciente. */
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "historico_id")
    private HistoricoMedico historicoMedico;

    @JsonIgnore
    @OneToMany(mappedBy = "paciente")
    private List<Atendimento> atendimentos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "paciente")
    private List<Internacao> internacoes = new ArrayList<>();

    protected Paciente() {
    }

    public Paciente(String nome, String telefone, String email, String cpf,
                    LocalDate dataNascimento, String endereco) {
        super(nome, telefone, email);
        this.cpf = normalizarCpf(cpf);
        exigir(this.cpf.length() == 11, "CPF deve ter 11 dígitos.");
        exigir(dataNascimento != null, "A data de nascimento é obrigatória.");
        exigir(!dataNascimento.isAfter(LocalDate.now()), "A data de nascimento não pode estar no futuro.");
        this.dataNascimento = dataNascimento;
        this.endereco = endereco;
        this.historicoMedico = new HistoricoMedico(this);
    }

    /** Mantém só os dígitos: "123.456.789-00" vira "12345678900". */
    public static String normalizarCpf(String cpf) {
        exigir(!vazio(cpf), "O CPF é obrigatório.");
        return cpf.replaceAll("\\D", "");
    }

    @JsonProperty("idade")
    public int calcularIdade() {
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public void adicionarAtendimento(Atendimento atendimento) {
        atendimentos.add(atendimento);
    }

    public void adicionarInternacao(Internacao internacao) {
        internacoes.add(internacao);
    }

    public boolean possuiInternacaoAtiva() {
        boolean possui = false;
        for (Internacao internacao : internacoes) {
            if (internacao.estaAtiva()) {
                possui = true;
            }
        }
        return possui;
    }

    /** Evita marcar dois atendimentos para o mesmo paciente no mesmo horário. */
    public boolean possuiAtendimentoNoHorario(LocalDateTime inicio, LocalDateTime fim) {
        boolean possui = false;
        for (Atendimento atendimento : atendimentos) {
            if (atendimento.ocupaAgenda() && atendimento.conflitaCom(inicio, fim)) {
                possui = true;
            }
        }
        return possui;
    }

    public boolean possuiRegistros() {
        return !atendimentos.isEmpty() || !internacoes.isEmpty();
    }

    public void atualizarEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getEndereco() {
        return endereco;
    }

    public HistoricoMedico getHistoricoMedico() {
        return historicoMedico;
    }

    public List<Atendimento> getAtendimentos() {
        return atendimentos;
    }

    public List<Internacao> getInternacoes() {
        return internacoes;
    }
}
