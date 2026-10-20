package br.pucminas.hospitalvida.model;

import static br.pucminas.hospitalvida.model.Validacao.exigir;
import static br.pucminas.hospitalvida.model.Validacao.vazio;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Realiza procedimentos cirúrgicos. */
@Entity
@DiscriminatorValue("CIRURGIAO")
public class Cirurgiao extends ProfissionalSaude {

    /** Informação da atuação cirúrgica, ex.: "Cirurgia geral", "Cirurgia cardíaca". */
    private String areaCirurgica;

    protected Cirurgiao() {
    }

    public Cirurgiao(String nome, String registroProfissional, String telefone, String email,
                     String areaCirurgica) {
        super(nome, registroProfissional, telefone, email);
        exigir(!vazio(areaCirurgica), "Informe a área de atuação cirúrgica do cirurgião.");
        this.areaCirurgica = areaCirurgica;
    }

    @Override
    public CategoriaProfissional getCategoria() {
        return CategoriaProfissional.CIRURGIAO;
    }

    @Override
    public boolean podeRealizarCirurgias() {
        return true;
    }

    /** Cria um procedimento cirúrgico sob responsabilidade deste cirurgião. */
    public ProcedimentoCirurgico registrarProcedimento(Paciente paciente, String descricao,
                                                       TipoCirurgia tipoCirurgia,
                                                       LocalDateTime dataHoraPrevista,
                                                       int duracaoPrevistaMinutos,
                                                       String observacoesPreOperatorias) {
        return new ProcedimentoCirurgico(paciente, this, descricao, tipoCirurgia, dataHoraPrevista,
                duracaoPrevistaMinutos, observacoesPreOperatorias);
    }

    /** Procedimentos (agendados, realizados ou cancelados) deste cirurgião. */
    public List<ProcedimentoCirurgico> listarProcedimentos() {
        List<ProcedimentoCirurgico> procedimentos = new ArrayList<>();
        for (Atendimento atendimento : getAtendimentos()) {
            if (atendimento instanceof ProcedimentoCirurgico procedimento) {
                procedimentos.add(procedimento);
            }
        }
        return procedimentos;
    }

    public void atualizarAreaCirurgica(String areaCirurgica) {
        exigir(!vazio(areaCirurgica), "Informe a área de atuação cirúrgica do cirurgião.");
        this.areaCirurgica = areaCirurgica;
    }

    public String getAreaCirurgica() {
        return areaCirurgica;
    }
}
