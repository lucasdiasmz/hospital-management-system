package br.pucminas.hospitalvida.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;
import br.pucminas.hospitalvida.model.enums.StatusProcedimento;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;

/** Testa as regras do domínio sem precisar subir o Spring nem o banco. */
class RegrasDeNegocioTest {

    // 07/01/2030 é uma segunda-feira, às 10h: horário válido para consultas
    private static final LocalDateTime SEGUNDA_10H = LocalDateTime.of(2030, 1, 7, 10, 0);

    private final Paciente paciente = new Paciente("Ana Souza", "31999990000", "ana@email.com",
            "123.456.789-00", LocalDate.of(1990, 5, 10), "Rua A, 10");
    private final ClinicoGeral clinico = new ClinicoGeral("Dr. Carlos", "CRM-MG 1001", "3133330000", "carlos@hv.com");
    private final MedicoEspecialista cardiologista = new MedicoEspecialista("Dra. Beatriz", "CRM-MG 2002",
            "3133331111", "bia@hv.com", Especialidade.CARDIOLOGIA);
    private final Cirurgiao cirurgiao = new Cirurgiao("Dr. Rafael", "CRM-MG 3003", "3133332222",
            "rafael@hv.com", "Cirurgia geral");

    // ---------------- Situação das consultas ----------------

    @Test
    void consultaSegueOFluxoCompletoAteConcluida() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up anual");
        consulta.validar();
        consulta.confirmar();
        consulta.iniciarAtendimento();
        consulta.concluir("Paciente saudável.");
        assertEquals(SituacaoConsulta.CONCLUIDA, consulta.getSituacao());
        assertEquals("Paciente saudável.", consulta.getObservacoes());
    }

    @Test
    void consultaConcluidaNaoVoltaParaAgendada() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        consulta.confirmar();
        consulta.iniciarAtendimento();
        consulta.concluir(null);
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.AGENDADA));
    }

    @Test
    void consultaCanceladaNaoPodeSerConcluida() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        consulta.cancelar();
        assertThrows(RegraNegocioException.class, () -> consulta.concluir("x"));
    }

    @Test
    void consultaCanceladaPodeSerReagendadaComoNovaConsulta() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        consulta.cancelar();
        Consulta nova = consulta.reagendar(SEGUNDA_10H.plusDays(1));
        assertEquals(SituacaoConsulta.AGENDADA, nova.getSituacao());
        assertEquals(SituacaoConsulta.CANCELADA, consulta.getSituacao());
        assertTrue(nova.getConsultaOriginal() == consulta);
    }

    @Test
    void naoReagendaConsultaQueAindaEstaAgendada() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        assertThrows(RegraNegocioException.class, () -> consulta.reagendar(SEGUNDA_10H.plusDays(1)));
    }

    @Test
    void naoRegistraObservacaoAntesDoAtendimentoComecar() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        assertThrows(RegraNegocioException.class, () -> consulta.registrarObservacoes("cedo demais"));
    }

    // ---------------- Modalidades de atendimento ----------------

    @Test
    void consultaDeRotinaExigeClinicoGeral() {
        ConsultaRotina comEspecialista = new ConsultaRotina(paciente, cardiologista, SEGUNDA_10H, "Check-up");
        assertThrows(RegraNegocioException.class, comEspecialista::validar);
    }

    @Test
    void consultaEspecializadaExigeEspecialidadeCompativel() {
        ConsultaEspecializada errada = new ConsultaEspecializada(paciente, cardiologista, SEGUNDA_10H,
                "Dor no joelho", Especialidade.ORTOPEDIA);
        assertThrows(RegraNegocioException.class, errada::validar);

        ConsultaEspecializada certa = new ConsultaEspecializada(paciente, cardiologista, SEGUNDA_10H,
                "Palpitações", Especialidade.CARDIOLOGIA);
        certa.validar();
    }

    @Test
    void clinicoGeralEncaminhaParaEspecialista() {
        ConsultaEspecializada consulta = clinico.encaminhar(paciente, cardiologista, Especialidade.CARDIOLOGIA,
                SEGUNDA_10H, "Sopro no coração");
        consulta.validar();
        assertTrue(consulta.getEncaminhadoPor() == clinico);
    }

    @Test
    void consultaNoDomingoOuForaDoHorarioEhRecusada() {
        LocalDateTime domingo = LocalDateTime.of(2030, 1, 6, 10, 0);
        assertThrows(RegraNegocioException.class,
                () -> new ConsultaRotina(paciente, clinico, domingo, "Check-up").validar());
        LocalDateTime tardeDemais = SEGUNDA_10H.withHour(18).withMinute(45);
        assertThrows(RegraNegocioException.class,
                () -> new ConsultaRotina(paciente, clinico, tardeDemais, "Check-up").validar());
    }

    @Test
    void emergenciaComecaAguardandoEDepoisEhAtendida() {
        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(paciente, Prioridade.CRITICA, "Dor no peito");
        emergencia.validar();
        assertTrue(emergencia.aguardandoAvaliacao());
        assertThrows(RegraNegocioException.class, () -> emergencia.concluir("pulou etapa"));
        emergencia.iniciarAvaliacao(clinico);
        emergencia.concluir("Estabilizado.");
        assertEquals(StatusEmergencia.CONCLUIDO, emergencia.getStatus());
    }

    @Test
    void emergenciaSemPrioridadeEhRecusada() {
        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(paciente, null, "Febre alta");
        assertThrows(RegraNegocioException.class, emergencia::validar);
    }

    // ---------------- Procedimentos cirúrgicos ----------------

    @Test
    void procedimentoNaoPodeSerConcluidoSemTerSidoIniciado() {
        ProcedimentoCirurgico procedimento = cirurgiao.registrarProcedimento(paciente, "Apendicectomia",
                TipoCirurgia.ELETIVA, SEGUNDA_10H, 90, "Jejum de 8h");
        procedimento.validar();
        assertThrows(RegraNegocioException.class, () -> procedimento.concluir("ok"));

        procedimento.iniciar();
        procedimento.concluir("Sem intercorrências.");
        assertEquals(StatusProcedimento.CONCLUIDO, procedimento.getStatus());
        assertNotNull(procedimento.getDataHoraInicioEfetivo());
        assertNotNull(procedimento.getDataHoraFimEfetivo());
    }

    @Test
    void procedimentoExigeCirurgiao() {
        ProcedimentoCirurgico procedimento = new ProcedimentoCirurgico(paciente, clinico, "Apendicectomia",
                TipoCirurgia.ELETIVA, SEGUNDA_10H, 90, null);
        assertThrows(RegraNegocioException.class, procedimento::validar);
    }

    @Test
    void procedimentoCanceladoNaoPodeSerIniciado() {
        ProcedimentoCirurgico procedimento = cirurgiao.registrarProcedimento(paciente, "Hérnia",
                TipoCirurgia.ELETIVA, SEGUNDA_10H, 60, null);
        procedimento.cancelar();
        assertThrows(RegraNegocioException.class, procedimento::iniciar);
    }

    // ---------------- Profissionais ----------------

    @Test
    void profissionalFicaIndisponivelNoHorarioDeUmaConsulta() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        clinico.adicionarAtendimento(consulta);
        assertFalse(clinico.estaDisponivel(SEGUNDA_10H.plusMinutes(15), SEGUNDA_10H.plusMinutes(45)));
        assertTrue(clinico.estaDisponivel(SEGUNDA_10H.plusMinutes(30), SEGUNDA_10H.plusMinutes(60)));

        consulta.cancelar();
        assertTrue(clinico.estaDisponivel(SEGUNDA_10H, SEGUNDA_10H.plusMinutes(30)));
    }

    @Test
    void somenteOResponsavelRegistraObservacoes() {
        ConsultaRotina consulta = new ConsultaRotina(paciente, clinico, SEGUNDA_10H, "Check-up");
        consulta.confirmar();
        consulta.iniciarAtendimento();
        assertThrows(RegraNegocioException.class, () -> cardiologista.registrarObservacao(consulta, "intruso"));
        clinico.registrarObservacao(consulta, "Pressão normal.");
        assertEquals("Pressão normal.", consulta.getObservacoes());
    }

    // ---------------- Quartos e internações ----------------

    @Test
    void tipoDoQuartoLimitaACapacidade() {
        assertThrows(RegraNegocioException.class, () -> new Quarto("101", 1, TipoQuarto.APARTAMENTO, 3));
        assertThrows(RegraNegocioException.class, () -> new Quarto("102", 1, TipoQuarto.ISOLAMENTO, 2));
    }

    @Test
    void situacaoDoQuartoAcompanhaAOcupacao() {
        Quarto quarto = new Quarto("201", 2, TipoQuarto.APARTAMENTO, 2);
        quarto.ocupar();
        assertEquals(SituacaoQuarto.PARCIALMENTE_OCUPADO, quarto.getSituacao());
        quarto.ocupar();
        assertEquals(SituacaoQuarto.LOTADO, quarto.getSituacao());
        assertThrows(RegraNegocioException.class, quarto::ocupar);
        quarto.liberar();
        quarto.liberar();
        assertEquals(SituacaoQuarto.DISPONIVEL, quarto.getSituacao());
    }

    @Test
    void internacaoEmUtiExigeJustificativa() {
        Quarto uti = new Quarto("UTI-1", 3, TipoQuarto.UTI, 8);
        Internacao semJustificativa = new Internacao(paciente, clinico, uti, null, null);
        assertThrows(RegraNegocioException.class, semJustificativa::iniciar);
        assertEquals(0, uti.getOcupacaoAtual());
    }

    @Test
    void pacienteNaoPodeTerDuasInternacoesAtivas() {
        Quarto quarto = new Quarto("301", 3, TipoQuarto.ENFERMARIA, 4);
        Internacao primeira = new Internacao(paciente, clinico, quarto, null, null);
        primeira.iniciar();
        paciente.adicionarInternacao(primeira);

        Internacao segunda = new Internacao(paciente, clinico, quarto, null, null);
        assertThrows(RegraNegocioException.class, segunda::iniciar);
    }

    @Test
    void transferenciaEAltaAtualizamOsQuartos() {
        Quarto origem = new Quarto("401", 4, TipoQuarto.ENFERMARIA, 4);
        Quarto destino = new Quarto("402", 4, TipoQuarto.APARTAMENTO, 1);
        Internacao internacao = new Internacao(paciente, clinico, origem, LocalDate.now().plusDays(3), null);
        internacao.iniciar();
        assertEquals(1, origem.getOcupacaoAtual());

        internacao.transferir(destino);
        assertEquals(0, origem.getOcupacaoAtual());
        assertEquals(SituacaoQuarto.LOTADO, destino.getSituacao());

        internacao.registrarAlta(LocalDate.now());
        assertEquals(StatusInternacao.ENCERRADA, internacao.getStatus());
        assertEquals(SituacaoQuarto.DISPONIVEL, destino.getSituacao());
        assertThrows(RegraNegocioException.class, () -> internacao.registrarAlta(LocalDate.now()));
    }

    @Test
    void naoBloqueiaQuartoOcupado() {
        Quarto quarto = new Quarto("501", 5, TipoQuarto.ENFERMARIA, 2);
        quarto.ocupar();
        assertThrows(RegraNegocioException.class, quarto::marcarIndisponivel);
    }

    // ---------------- Paciente ----------------

    @Test
    void pacienteValidaCpfEEmail() {
        assertThrows(RegraNegocioException.class, () -> new Paciente("João", null, null, "123",
                LocalDate.of(2000, 1, 1), null));
        assertThrows(RegraNegocioException.class, () -> new Paciente("João", null, "email-sem-arroba",
                "11122233344", LocalDate.of(2000, 1, 1), null));
        assertEquals("12345678900", paciente.getCpf());
        assertNotNull(paciente.getHistoricoMedico());
    }
}
