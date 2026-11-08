package br.pucminas.hospitalvida.model;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/**
 * Realiza procedimentos cirúrgicos. Além dos dados comuns, guarda as informações da
 * sua atuação cirúrgica. Os procedimentos em si (data, paciente, descrição) ficam na
 * classe ProcedimentoCirurgico (Pessoa 4), que aponta para o cirurgião responsável.
 */
@Entity
@DiscriminatorValue("CIRURGIAO")
public class Cirurgiao extends Profissional {

    /** Ex.: "Cirurgia geral", "Cirurgia cardiovascular". */
    private String areaCirurgica;

    /** Registro de Qualificação de Especialista (RQE) da área cirúrgica. */
    private String rqe;

    protected Cirurgiao() {
    }

    public Cirurgiao(String nome, String registroProfissional, String telefone, String email,
                     String areaCirurgica, String rqe) {
        super(nome, registroProfissional, telefone, email);
        atualizarAtuacaoCirurgica(areaCirurgica, rqe);
    }

    @Override
    public CategoriaProfissional getCategoria() {
        return CategoriaProfissional.CIRURGIAO;
    }

    @Override
    public boolean podeRealizarCirurgia() {
        return true;
    }

    public void atualizarAtuacaoCirurgica(String areaCirurgica, String rqe) {
        exigir(!vazio(areaCirurgica), "Informe a área de atuação cirúrgica do cirurgião.");
        exigir(!vazio(rqe), "Informe o RQE (registro de qualificação) do cirurgião.");
        this.areaCirurgica = areaCirurgica.trim();
        this.rqe = rqe.trim();
    }

    public String getAreaCirurgica() {
        return areaCirurgica;
    }

    public String getRqe() {
        return rqe;
    }
}
