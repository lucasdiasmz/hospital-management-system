package br.pucminas.hospitalvida.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;
import br.pucminas.hospitalvida.model.enums.StatusProcedimento;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;

/** Regras de procedimentos cirúrgicos, quartos e internações, testadas sem subir o Spring. */
class CirurgiaInternacaoTest {

    private static final LocalDate AMANHA = LocalDate.now().plusDays(1);
    private static final LocalDate HOJE = LocalDate.now();
    private static final LocalTime DEZ_HORAS = LocalTime.of(10, 0);

    private final Paciente paciente = new Paciente();
    private final Cirurgiao cirurgiao = new Cirurgiao("Dr. Rafael", "CRM-MG 3003", null, null, "Cirurgia geral", "RQE 1");
    private final MedicoClinicoGeral clinico = new MedicoClinicoGeral("Dr. Carlos", "CRM-MG 1001", null, null);
    private final MedicoEspecialista cardiologista =
            new MedicoEspecialista("Dra. Beatriz", "CRM-MG 2002", null, null, Especialidade.CARDIOLOGIA);
    private final Quarto enfermaria = new Quarto("101", 1, TipoQuarto.ENFERMARIA, 2);
    private final Quarto uti = new Quarto("UTI-1", 3, TipoQuarto.UTI, 1);

    private ProcedimentoCirurgico procedimento(Profissional responsavel) {
        return new ProcedimentoCirurgico(AMANHA, DEZ_HORAS, paciente, responsavel, "Apendicectomia",
                TipoCirurgia.ELETIVA, "Jejum de 8 horas");
    }

    private Internacao internacao(Quarto quarto, String observacoes) {
        return new Internacao(paciente, clinico, quarto, AMANHA, observacoes);
    }

    // ---------------- Procedimento cirúrgico ----------------

    @Test
    void procedimentoSegueOFluxoERegistraAsDatasEfetivas() {
        ProcedimentoCirurgico procedimento = procedimento(cirurgiao);
        assertEquals(StatusProcedimento.AGENDADO, procedimento.getStatus());

        procedimento.iniciar();
        assertEquals(StatusProcedimento.EM_ANDAMENTO, procedimento.getStatus());
        assertNotNull(procedimento.getInicioEfetivo());

        procedimento.concluir("Sem intercorrências");
        assertEquals(StatusProcedimento.CONCLUIDO, procedimento.getStatus());
        assertNotNull(procedimento.getFimEfetivo());
        assertEquals("Sem intercorrências", procedimento.getObservacoesPosOperatorias());
    }

    @Test
    void naoConcluiProcedimentoQueNaoFoiIniciado() {
        ProcedimentoCirurgico procedimento = procedimento(cirurgiao);
        assertThrows(RegraNegocioException.class, () -> procedimento.concluir("x"));
        assertEquals(StatusProcedimento.AGENDADO, procedimento.getStatus());
    }

    @Test
    void procedimentoCanceladoOuConcluidoNaoMudaMais() {
        ProcedimentoCirurgico cancelado = procedimento(cirurgiao);
        cancelado.cancelar();
        assertThrows(RegraNegocioException.class, cancelado::iniciar);

        ProcedimentoCirurgico concluido = procedimento(cirurgiao);
        concluido.iniciar();
        concluido.concluir(null);
        assertThrows(RegraNegocioException.class, concluido::cancelar);
    }

    @Test
    void responsavelPrecisaSerCirurgiaoAtivo() {
        procedimento(cirurgiao).validarRealizacao();
        assertThrows(RegraNegocioException.class, () -> procedimento(clinico).validarRealizacao());
        assertThrows(RegraNegocioException.class, () -> procedimento(cardiologista).validarRealizacao());
        assertThrows(RegraNegocioException.class, () -> procedimento(null).validarRealizacao());
        cirurgiao.desativar();
        assertThrows(RegraNegocioException.class, () -> procedimento(cirurgiao).validarRealizacao());
    }

    @Test
    void procedimentoExigeDescricaoETipo() {
        assertThrows(RegraNegocioException.class, () -> new ProcedimentoCirurgico(AMANHA, DEZ_HORAS, paciente, cirurgiao,
                " ", TipoCirurgia.ELETIVA, null).validarRealizacao());
        assertThrows(RegraNegocioException.class, () -> new ProcedimentoCirurgico(AMANHA, DEZ_HORAS, paciente, cirurgiao,
                "Apendicectomia", null, null).validarRealizacao());
    }

    @Test
    void somenteCirurgiaEletivaPrecisaSerNoFuturo() {
        LocalDate ontem = LocalDate.now().minusDays(1);
        assertThrows(RegraNegocioException.class, () -> new ProcedimentoCirurgico(ontem, DEZ_HORAS, paciente, cirurgiao,
                "Apendicectomia", TipoCirurgia.ELETIVA, null).validarAgendamento());
        new ProcedimentoCirurgico(ontem, DEZ_HORAS, paciente, cirurgiao, "Apendicectomia",
                TipoCirurgia.EMERGENCIA, null).validarAgendamento();
    }

    // ---------------- Quarto ----------------

    @Test
    void cadaTipoDeQuartoLimitaACapacidade() {
        assertThrows(RegraNegocioException.class, () -> new Quarto("1", 1, TipoQuarto.ISOLAMENTO, 2));
        assertThrows(RegraNegocioException.class, () -> new Quarto("1", 1, TipoQuarto.APARTAMENTO, 3));
        assertThrows(RegraNegocioException.class, () -> new Quarto("1", 1, TipoQuarto.ENFERMARIA, 0));
        assertThrows(RegraNegocioException.class, () -> new Quarto("1", 1, null, 1));
        assertThrows(RegraNegocioException.class, () -> new Quarto(" ", 1, TipoQuarto.UTI, 1));
    }

    @Test
    void situacaoAcompanhaAOcupacao() {
        assertEquals(SituacaoQuarto.DISPONIVEL, enfermaria.getSituacao());
        enfermaria.ocupar();
        assertEquals(SituacaoQuarto.PARCIALMENTE_OCUPADO, enfermaria.getSituacao());
        enfermaria.ocupar();
        assertEquals(SituacaoQuarto.LOTADO, enfermaria.getSituacao());
        assertFalse(enfermaria.possuiVaga());
        assertThrows(RegraNegocioException.class, enfermaria::ocupar);
        enfermaria.liberar();
        enfermaria.liberar();
        assertEquals(SituacaoQuarto.DISPONIVEL, enfermaria.getSituacao());
        assertThrows(RegraNegocioException.class, enfermaria::liberar);
    }

    @Test
    void quartoOcupadoNaoPodeSerBloqueadoEBloqueadoNaoRecebePaciente() {
        enfermaria.ocupar();
        assertThrows(RegraNegocioException.class, enfermaria::bloquear);
        enfermaria.liberar();
        enfermaria.bloquear();
        assertFalse(enfermaria.possuiVaga());
        assertThrows(RegraNegocioException.class, enfermaria::ocupar);
        enfermaria.desbloquear();
        assertTrue(enfermaria.possuiVaga());
    }

    // ---------------- Internação ----------------

    @Test
    void internacaoOcupaELiberaOLeito() {
        Internacao internacao = internacao(enfermaria, null);
        internacao.iniciar();
        assertEquals(StatusInternacao.ATIVA, internacao.getStatus());
        assertEquals(1, enfermaria.getOcupacaoAtual());
        assertThrows(RegraNegocioException.class, internacao::iniciar);

        internacao.registrarAlta(HOJE);
        assertEquals(StatusInternacao.ENCERRADA, internacao.getStatus());
        assertEquals(HOJE, internacao.getDataEfetivaAlta());
        assertEquals(0, enfermaria.getOcupacaoAtual());
        assertThrows(RegraNegocioException.class, () -> internacao.registrarAlta(HOJE));
    }

    @Test
    void altaRecusaDataInvalida() {
        Internacao internacao = internacao(enfermaria, null);
        internacao.iniciar();
        assertThrows(RegraNegocioException.class, () -> internacao.registrarAlta(HOJE.minusDays(1)));
        assertThrows(RegraNegocioException.class, () -> internacao.registrarAlta(AMANHA));
        assertThrows(RegraNegocioException.class, () -> internacao.registrarAlta(null));
    }

    @Test
    void cancelamentoLiberaOLeito() {
        Internacao internacao = internacao(enfermaria, null);
        internacao.iniciar();
        internacao.cancelar();
        assertEquals(StatusInternacao.CANCELADA, internacao.getStatus());
        assertEquals(0, enfermaria.getOcupacaoAtual());
    }

    @Test
    void utiExigeJustificativa() {
        assertThrows(RegraNegocioException.class, () -> internacao(uti, " ").iniciar());
        assertEquals(0, uti.getOcupacaoAtual());
        internacao(uti, "Insuficiência respiratória").iniciar();
        assertEquals(1, uti.getOcupacaoAtual());
    }

    @Test
    void naoInternaEmQuartoLotadoNemComMedicoDesativado() {
        internacao(uti, "Motivo").iniciar();
        assertThrows(RegraNegocioException.class, () -> internacao(uti, "Motivo").iniciar());
        clinico.desativar();
        assertThrows(RegraNegocioException.class, () -> internacao(enfermaria, null).iniciar());
    }

    @Test
    void transferenciaMoveOLeitoEFalhaSemMudarNadaSeODestinoEstiverLotado() {
        Internacao internacao = internacao(enfermaria, "Piora clínica");
        internacao.iniciar();
        internacao.transferir(uti);
        assertEquals(uti, internacao.getQuarto());
        assertEquals(0, enfermaria.getOcupacaoAtual());
        assertEquals(1, uti.getOcupacaoAtual());
        assertThrows(RegraNegocioException.class, () -> internacao.transferir(uti));

        Quarto lotado = new Quarto("103", 1, TipoQuarto.APARTAMENTO, 1);
        lotado.ocupar();
        assertThrows(RegraNegocioException.class, () -> internacao.transferir(lotado));
        assertEquals(uti, internacao.getQuarto());
        assertEquals(1, uti.getOcupacaoAtual());
    }

    @Test
    void transferenciaParaUtiExigeJustificativa() {
        Internacao internacao = internacao(enfermaria, null);
        internacao.iniciar();
        assertThrows(RegraNegocioException.class, () -> internacao.transferir(uti));
        assertEquals(enfermaria, internacao.getQuarto());
    }
}
