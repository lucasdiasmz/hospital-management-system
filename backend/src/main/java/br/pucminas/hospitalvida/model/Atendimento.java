package br.pucminas.hospitalvida.model;

import java.time.LocalDate;
import java.time.LocalTime;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Dados e comportamentos comuns a todas as modalidades de atendimento.
 *
 * Cada modalidade implementa validarRealizacao() com as condições próprias dela.
 *
 * No banco, todas as modalidades ficam na mesma tabela (SINGLE_TABLE), diferenciadas
 * pela coluna "tipo_atendimento". Por isso, duas modalidades não podem usar o mesmo nome
 * de coluna com tipos diferentes (ver AtendimentoEmergencia.status).
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_atendimento")
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate data;
    private LocalTime horario;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    private Profissional profissional;

    @Column(length = 2000)
    private String observacoes;

    protected Atendimento() {
        // exigido pelo JPA
    }

    protected Atendimento(LocalDate data, LocalTime horario, Paciente paciente, Profissional profissional,
                          String observacoes) {
        this.data = data;
        this.horario = horario;
        this.paciente = paciente;
        this.profissional = profissional;
        this.observacoes = vazio(observacoes) ? null : observacoes.trim();
    }

    // ---------- Comportamentos que cada modalidade define ----------

    /** Lança RegraNegocioException se faltar alguma condição para o atendimento acontecer. */
    public abstract void validarRealizacao();

    /** Modalidade do atendimento (campo "tipo" do JSON). */
    public abstract String getTipo();

    /** Em que momento do atendimento é permitido registrar observações. */
    protected abstract boolean podeReceberObservacoes();

    // ---------- Comportamentos comuns ----------

    /** Acrescenta a observação às anteriores, sem apagar o que já foi registrado. */
    public void registrarObservacoes(String texto) {
        exigir(!vazio(texto), "Informe o texto da observação.");
        exigir(podeReceberObservacoes(), "Não é possível registrar observações neste momento do atendimento.");
        this.observacoes = vazio(observacoes) ? texto.trim() : observacoes + "\n" + texto.trim();
    }

    protected void validarDadosComuns() {
        exigir(data != null, "Informe a data do atendimento.");
        exigir(horario != null, "Informe o horário do atendimento.");
        exigir(paciente != null, "Informe o paciente do atendimento.");
    }

    protected void definirProfissional(Profissional profissional) {
        this.profissional = profissional;
    }

    // ---------- Atalhos de validação (também usados pelas subclasses) ----------

    protected static void exigir(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new RegraNegocioException(mensagem);
        }
    }

    protected static boolean vazio(String texto) {
        return texto == null || texto.isBlank();
    }

    // ---------- Getters ----------

    public Long getId() {
        return id;
    }

    public LocalDate getData() {
        return data;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public Profissional getProfissional() {
        return profissional;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
