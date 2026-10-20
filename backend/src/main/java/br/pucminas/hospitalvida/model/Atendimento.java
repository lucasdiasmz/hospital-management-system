package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDateTime;

import br.pucminas.hospitalvida.model.enums.Especialidade;
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
 * Tudo o que as modalidades de atendimento têm em comum fica aqui
 * (paciente, profissional, data/hora, observações), sem duplicação.
 *
 * O método validar() é um "template method": ele faz as checagens comuns
 * e depois chama validarCondicoes(), que cada modalidade implementa do seu jeito.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_atendimento")
public abstract class Atendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "profissional_id")
    protected ProfissionalSaude profissional;

    private LocalDateTime dataHora;

    @Column(length = 2000)
    private String observacoes;

    protected Atendimento() {
    }

    protected Atendimento(Paciente paciente, ProfissionalSaude profissional, LocalDateTime dataHora) {
        this.paciente = paciente;
        this.profissional = profissional;
        this.dataHora = dataHora;
    }

    /** Valida as condições necessárias para o atendimento acontecer. */
    public void validar() {
        exigir(paciente != null, "O atendimento precisa estar associado a um paciente.");
        exigir(dataHora != null, "A data e o horário do atendimento são obrigatórios.");
        validarCondicoes();
    }

    /** Regras específicas de cada modalidade. */
    protected abstract void validarCondicoes();

    /** Nome da modalidade, usado na resposta JSON (campo "tipo"). */
    public abstract String getTipo();

    /** Quanto tempo o atendimento ocupa na agenda do profissional. */
    public abstract int getDuracaoEstimadaMinutos();

    /** Se o atendimento ainda reserva o horário do profissional. */
    public abstract boolean ocupaAgenda();

    /** Em que momento do atendimento é permitido registrar observações. */
    protected abstract boolean podeReceberObservacoes();

    public LocalDateTime getDataHoraFimPrevista() {
        return dataHora.plusMinutes(getDuracaoEstimadaMinutos());
    }

    /** Dois intervalos se sobrepõem se um começa antes do outro terminar. */
    public boolean conflitaCom(LocalDateTime inicio, LocalDateTime fim) {
        return dataHora.isBefore(fim) && getDataHoraFimPrevista().isAfter(inicio);
    }

    public void registrarObservacoes(String texto) {
        exigir(!vazio(texto), "O texto da observação não pode ser vazio.");
        exigir(podeReceberObservacoes(),
                "Não é possível registrar observações neste momento do atendimento.");
        this.observacoes = texto;
    }

    /** Usado pelas subclasses para exigir que o profissional atenda a especialidade. */
    protected void exigirEspecialidade(Especialidade especialidade) {
        exigir(profissional.atendeEspecialidade(especialidade),
                "O profissional " + profissional.getNome() + " não atende a especialidade " + especialidade + ".");
    }

    public Long getId() {
        return id;
    }

    public Paciente getPaciente() {
        return paciente;
    }

    public ProfissionalSaude getProfissional() {
        return profissional;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public String getObservacoes() {
        return observacoes;
    }
}
