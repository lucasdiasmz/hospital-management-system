package br.pucminas.hospitalvida.model;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Realiza consultas de rotina e avaliações iniciais, e pode encaminhar pacientes para especialistas. */
@Entity
@DiscriminatorValue("CLINICO_GERAL")
public class MedicoClinicoGeral extends Profissional {

    protected MedicoClinicoGeral() {
    }

    public MedicoClinicoGeral(String nome, String registroProfissional, String telefone, String email) {
        super(nome, registroProfissional, telefone, email);
    }

    @Override
    public CategoriaProfissional getCategoria() {
        return CategoriaProfissional.CLINICO_GERAL;
    }

    @Override
    public boolean podeRealizarConsultaRotina() {
        return true;
    }

    /**
     * Encaminha o paciente para uma especialidade.
     * O especialista é opcional: se for indicado, precisa atender aquela especialidade.
     */
    public Encaminhamento encaminhar(Paciente paciente, Especialidade especialidade,
                                     Profissional especialistaIndicado, String motivo) {
        exigirAtivo();
        exigir(especialistaIndicado == null || especialistaIndicado.isAtivo(),
                "O especialista indicado está desativado.");
        return new Encaminhamento(paciente, this, especialidade, especialistaIndicado, motivo);
    }
}
