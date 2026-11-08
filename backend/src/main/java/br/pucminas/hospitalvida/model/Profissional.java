package br.pucminas.hospitalvida.model;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

/**
 * Dados e comportamentos comuns a todos os profissionais de saúde.
 *
 * É abstrata porque não existe um "profissional genérico": todo profissional é
 * MedicoClinicoGeral, MedicoEspecialista ou Cirurgiao.
 *
 * As outras partes do sistema perguntam ao profissional o que ele pode fazer, em vez de
 * testar o tipo dele (polimorfismo):
 *   - podeRealizarConsultaRotina()   -> usado pela ConsultaRotina (Pessoa 3)
 *   - atendeEspecialidade(e)         -> usado pela ConsultaEspecializada (Pessoa 3)
 *   - podeRealizarCirurgia()         -> usado pelo ProcedimentoCirurgico (Pessoa 4)
 *
 * No banco, as três categorias ficam na mesma tabela (SINGLE_TABLE), diferenciadas
 * pela coluna "categoria".
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "categoria")
public abstract class Profissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true)
    private String registroProfissional;

    private String telefone;
    private String email;

    /** Profissional desligado não aparece na busca e não pode receber novos atendimentos. */
    private boolean ativo;

    protected Profissional() {
        // exigido pelo JPA
    }

    protected Profissional(String nome, String registroProfissional, String telefone, String email) {
        exigir(!vazio(registroProfissional), "O registro profissional (CRM) é obrigatório.");
        this.registroProfissional = registroProfissional.trim();
        atualizarNome(nome);
        atualizarContato(telefone, email);
        this.ativo = true;
    }

    // ---------- Comportamentos que cada categoria sobrescreve ----------

    public abstract CategoriaProfissional getCategoria();

    /** Por padrão, ninguém realiza consulta de rotina; o clínico geral sobrescreve. */
    public boolean podeRealizarConsultaRotina() {
        return false;
    }

    /** Por padrão, ninguém atende especialidade; o especialista sobrescreve. */
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return false;
    }

    /** Por padrão, ninguém opera; o cirurgião sobrescreve. */
    public boolean podeRealizarCirurgia() {
        return false;
    }

    // ---------- Comportamentos comuns ----------

    public void atualizarNome(String nome) {
        exigir(!vazio(nome), "O nome é obrigatório.");
        this.nome = nome.trim();
    }

    /** E-mail é opcional; se for informado, precisa ter o formato algo@dominio.algo */
    public void atualizarContato(String telefone, String email) {
        exigir(emailValido(email), "E-mail inválido: " + email);
        this.telefone = telefone;
        this.email = email;
    }

    public void desativar() {
        exigir(ativo, "O profissional já está desativado.");
        this.ativo = false;
    }

    public void reativar() {
        exigir(!ativo, "O profissional já está ativo.");
        this.ativo = true;
    }

    /** Usado pelas outras partes antes de criar um atendimento com este profissional. */
    public void exigirAtivo() {
        exigir(ativo, "O profissional " + nome + " está desativado e não pode receber atendimentos.");
    }

    public static boolean emailValido(String email) {
        return vazio(email) || email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
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

    public String getNome() {
        return nome;
    }

    public String getRegistroProfissional() {
        return registroProfissional;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
