package br.pucminas.hospitalvida.model;

import java.time.LocalDateTime;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Realiza consultas de rotina e pode encaminhar pacientes para especialistas. */
@Entity
@DiscriminatorValue("CLINICO_GERAL")
public class ClinicoGeral extends ProfissionalSaude {

    protected ClinicoGeral() {
    }

    public ClinicoGeral(String nome, String registroProfissional, String telefone, String email) {
        super(nome, registroProfissional, telefone, email);
    }

    @Override
    public CategoriaProfissional getCategoria() {
        return CategoriaProfissional.CLINICO_GERAL;
    }

    @Override
    public boolean realizaConsultaRotina() {
        return true;
    }

    /**
     * Encaminha o paciente para um especialista, gerando uma consulta especializada
     * que guarda quem fez o encaminhamento.
     */
    public ConsultaEspecializada encaminhar(Paciente paciente, ProfissionalSaude especialista,
                                            Especialidade especialidade, LocalDateTime dataHora,
                                            String motivo) {
        ConsultaEspecializada consulta =
                new ConsultaEspecializada(paciente, especialista, dataHora, motivo, especialidade);
        consulta.definirEncaminhamento(this);
        return consulta;
    }
}
