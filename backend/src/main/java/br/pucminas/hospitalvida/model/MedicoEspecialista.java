package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/** Atua em uma área específica e realiza consultas especializadas dessa área. */
@Entity
@DiscriminatorValue("ESPECIALISTA")
public class MedicoEspecialista extends ProfissionalSaude {

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    protected MedicoEspecialista() {
    }

    public MedicoEspecialista(String nome, String registroProfissional, String telefone, String email,
                              Especialidade especialidade) {
        super(nome, registroProfissional, telefone, email);
        exigir(especialidade != null, "O médico especialista precisa ter uma especialidade.");
        this.especialidade = especialidade;
    }

    @Override
    public CategoriaProfissional getCategoria() {
        return CategoriaProfissional.ESPECIALISTA;
    }

    @Override
    public boolean atendeEspecialidade(Especialidade especialidade) {
        return this.especialidade == especialidade;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }
}
