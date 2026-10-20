package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * Dados comuns a pacientes e profissionais.
 * É abstrata: não existe "uma pessoa" no sistema, só Paciente ou ProfissionalSaude.
 */
@MappedSuperclass
public abstract class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    protected String nome;
    protected String telefone;
    protected String email;

    protected Pessoa() {
        // exigido pelo JPA
    }

    protected Pessoa(String nome, String telefone, String email) {
        exigir(!vazio(nome), "O nome é obrigatório.");
        this.nome = nome;
        this.telefone = telefone;
        this.email = email;
        exigir(validarEmail(), "E-mail inválido: " + email);
    }

    public void atualizarContato(String telefone, String email) {
        String emailAnterior = this.email;
        this.email = email;
        if (!validarEmail()) {
            this.email = emailAnterior;
            exigir(false, "E-mail inválido: " + email);
        }
        this.telefone = telefone;
    }

    /** E-mail é opcional; se for informado, precisa ter o formato algo@dominio.algo */
    public boolean validarEmail() {
        return vazio(email) || email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    public void atualizarNome(String nome) {
        exigir(!vazio(nome), "O nome é obrigatório.");
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }
}
