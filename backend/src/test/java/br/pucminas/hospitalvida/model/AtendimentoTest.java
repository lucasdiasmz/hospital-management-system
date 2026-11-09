package br.pucminas.hospitalvida.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;

/** Regras das consultas e emergências, testadas sem subir o Spring. */
class AtendimentoTest {

    private static final LocalDate AMANHA = LocalDate.now().plusDays(1);
    private static final LocalTime DEZ_HORAS = LocalTime.of(10, 0);

    private final Paciente paciente = new Paciente();
    private final MedicoClinicoGeral clinico =
            new MedicoClinicoGeral("Dr. Carlos", "CRM-MG 1001", "3133330000", "carlos@hv.com");
    private final MedicoEspecialista cardiologista = new MedicoEspecialista("Dra. Beatriz", "CRM-MG 2002",
            "3133331111", "bia@hv.com", Especialidade.CARDIOLOGIA);
    private final Cirurgiao cirurgiao = new Cirurgiao("Dr. Rafael", "CRM-MG 3003", "3133332222",
            "rafael@hv.com", "Cirurgia geral", "RQE 12345");

    private ConsultaRotina rotina(Profissional profissional) {
        return new ConsultaRotina(AMANHA, DEZ_HORAS, paciente, profissional, "Check-up", null);
    }

    private ConsultaEspecializada especializada(Profissional profissional, Especialidade especialidade) {
        return new ConsultaEspecializada(AMANHA, DEZ_HORAS, paciente, profissional, "Sopro", null, especialidade, null);
    }

    private ConsultaRotina rotinaNaSituacao(SituacaoConsulta... caminho) {
        ConsultaRotina consulta = rotina(clinico);
        for (SituacaoConsulta situacao : caminho) {
            consulta.alterarSituacao(situacao);
        }
        return consulta;
    }

    // ---------------- Situação da consulta ----------------

    @Test
    void consultaComecaAgendadaESegueOFluxoAteConcluida() {
        ConsultaRotina consulta = rotina(clinico);
        assertEquals(SituacaoConsulta.AGENDADA, consulta.getSituacao());
        consulta.alterarSituacao(SituacaoConsulta.CONFIRMADA);
        consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO);
        consulta.alterarSituacao(SituacaoConsulta.CONCLUIDA);
        assertEquals(SituacaoConsulta.CONCLUIDA, consulta.getSituacao());
    }

    @Test
    void consultaConcluidaNaoVoltaParaAgendada() {
        ConsultaRotina consulta = rotinaNaSituacao(SituacaoConsulta.CONFIRMADA, SituacaoConsulta.EM_ATENDIMENTO,
                SituacaoConsulta.CONCLUIDA);
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.AGENDADA));
        assertEquals(SituacaoConsulta.CONCLUIDA, consulta.getSituacao());
    }

    @Test
    void consultaCanceladaNaoPodeSerConcluida() {
        ConsultaRotina consulta = rotinaNaSituacao(SituacaoConsulta.CANCELADA);
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.CONCLUIDA));
    }

    @Test
    void naoPulaEtapasDoAtendimento() {
        ConsultaRotina consulta = rotina(clinico);
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.CONCLUIDA));
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO));
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(null));
    }

    @Test
    void consultaEmAtendimentoNaoPodeSerCancelada() {
        ConsultaRotina consulta = rotinaNaSituacao(SituacaoConsulta.CONFIRMADA, SituacaoConsulta.EM_ATENDIMENTO);
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.CANCELADA));
    }

    @Test
    void medicoDesativadoNaoIniciaConsulta() {
        ConsultaRotina consulta = rotinaNaSituacao(SituacaoConsulta.CONFIRMADA);
        clinico.desativar();
        assertThrows(RegraNegocioException.class, () -> consulta.alterarSituacao(SituacaoConsulta.EM_ATENDIMENTO));
        assertEquals(SituacaoConsulta.CONFIRMADA, consulta.getSituacao());
    }

    // ---------------- Reagendamento ----------------

    @Test
    void consultaCanceladaGeraNovoAgendamentoEContinuaCancelada() {
        ConsultaRotina original = rotinaNaSituacao(SituacaoConsulta.CANCELADA);
        Consulta nova = original.reagendar(AMANHA.plusDays(1), DEZ_HORAS);
        assertEquals(SituacaoConsulta.AGENDADA, nova.getSituacao());
        assertEquals(SituacaoConsulta.CANCELADA, original.getSituacao());
        assertEquals(AMANHA.plusDays(1), nova.getData());
    }

    @Test
    void naoReagendaConsultaAtivaNemParaOPassado() {
        assertThrows(RegraNegocioException.class, () -> rotina(clinico).reagendar(AMANHA, DEZ_HORAS));
        ConsultaRotina cancelada = rotinaNaSituacao(SituacaoConsulta.CANCELADA);
        assertThrows(RegraNegocioException.class, () -> cancelada.reagendar(LocalDate.now().minusDays(1), DEZ_HORAS));
    }

    // ---------------- validarRealizacao em cada modalidade ----------------

    @Test
    void consultaDeRotinaExigeClinicoGeral() {
        rotina(clinico).validarRealizacao();
        assertThrows(RegraNegocioException.class, () -> rotina(cardiologista).validarRealizacao());
        assertThrows(RegraNegocioException.class, () -> rotina(cirurgiao).validarRealizacao());
    }

    @Test
    void consultaEspecializadaExigeProfissionalCompativel() {
        especializada(cardiologista, Especialidade.CARDIOLOGIA).validarRealizacao();
        assertThrows(RegraNegocioException.class,
                () -> especializada(cardiologista, Especialidade.ORTOPEDIA).validarRealizacao());
        assertThrows(RegraNegocioException.class,
                () -> especializada(clinico, Especialidade.CARDIOLOGIA).validarRealizacao());
        assertThrows(RegraNegocioException.class, () -> especializada(cardiologista, null).validarRealizacao());
    }

    @Test
    void consultaExigeDadosObrigatoriosEMedicoAtivo() {
        assertThrows(RegraNegocioException.class, () -> rotina(null).validarRealizacao());
        assertThrows(RegraNegocioException.class,
                () -> new ConsultaRotina(null, DEZ_HORAS, paciente, clinico, "Motivo", null).validarRealizacao());
        assertThrows(RegraNegocioException.class,
                () -> new ConsultaRotina(AMANHA, null, paciente, clinico, "Motivo", null).validarRealizacao());
        assertThrows(RegraNegocioException.class,
                () -> new ConsultaRotina(AMANHA, DEZ_HORAS, null, clinico, "Motivo", null).validarRealizacao());
        assertThrows(RegraNegocioException.class,
                () -> new ConsultaRotina(AMANHA, DEZ_HORAS, paciente, clinico, " ", null).validarRealizacao());
        clinico.desativar();
        assertThrows(RegraNegocioException.class, () -> rotina(clinico).validarRealizacao());
    }

    @Test
    void consultaNaoPodeSerAgendadaNoPassado() {
        ConsultaRotina ontem = new ConsultaRotina(LocalDate.now().minusDays(1), DEZ_HORAS, paciente, clinico, "Motivo", null);
        assertThrows(RegraNegocioException.class, ontem::validarAgendamento);
        rotina(clinico).validarAgendamento();
    }

    @Test
    void encaminhamentoPrecisaSerDoPacienteEDaEspecialidadeDaConsulta() {
        paciente.setId(1L);
        Encaminhamento encaminhamento = clinico.encaminhar(paciente, Especialidade.CARDIOLOGIA, null, "Sopro");
        new ConsultaEspecializada(AMANHA, DEZ_HORAS, paciente, cardiologista, "Sopro", null,
                Especialidade.CARDIOLOGIA, encaminhamento).validarRealizacao();

        Paciente outro = new Paciente();
        outro.setId(2L);
        assertThrows(RegraNegocioException.class, () -> new ConsultaEspecializada(AMANHA, DEZ_HORAS, outro, cardiologista,
                "Sopro", null, Especialidade.CARDIOLOGIA, encaminhamento).validarRealizacao());

        MedicoEspecialista ortopedista = new MedicoEspecialista("Dr. Marcos", "CRM-MG 4004", null, null, Especialidade.ORTOPEDIA);
        assertThrows(RegraNegocioException.class, () -> new ConsultaEspecializada(AMANHA, DEZ_HORAS, paciente, ortopedista,
                "Joelho", null, Especialidade.ORTOPEDIA, encaminhamento).validarRealizacao());
    }

    // ---------------- Observações ----------------

    @Test
    void observacoesSoDepoisQueOAtendimentoComeca() {
        ConsultaRotina consulta = rotina(clinico);
        assertThrows(RegraNegocioException.class, () -> consulta.registrarObservacoes("Antes da hora"));
        ConsultaRotina emAtendimento = rotinaNaSituacao(SituacaoConsulta.CONFIRMADA, SituacaoConsulta.EM_ATENDIMENTO);
        assertThrows(RegraNegocioException.class, () -> emAtendimento.registrarObservacoes(" "));
        emAtendimento.registrarObservacoes("Pressão normal");
        emAtendimento.alterarSituacao(SituacaoConsulta.CONCLUIDA, "Retorno em 6 meses");
        assertEquals("Pressão normal\nRetorno em 6 meses", emAtendimento.getObservacoes());
    }

    // ---------------- Emergência ----------------

    private AtendimentoEmergencia emergencia() {
        return new AtendimentoEmergencia(paciente, Prioridade.ALTA, "Dor no peito");
    }

    @Test
    void emergenciaComecaAguardandoESemMedico() {
        AtendimentoEmergencia emergencia = emergencia();
        emergencia.validarRealizacao();
        assertEquals(StatusEmergencia.AGUARDANDO, emergencia.getStatus());
        assertNull(emergencia.getProfissional());
    }

    @Test
    void emergenciaSegueOFluxoEDepoisNaoMuda() {
        AtendimentoEmergencia emergencia = emergencia();
        assertThrows(RegraNegocioException.class, () -> emergencia.concluir(null));
        emergencia.iniciar(clinico);
        assertEquals(StatusEmergencia.INICIADO, emergencia.getStatus());
        assertTrue(emergencia.getProfissional() == clinico);
        assertThrows(RegraNegocioException.class, () -> emergencia.iniciar(clinico));
        emergencia.concluir("Estabilizado");
        assertEquals(StatusEmergencia.CONCLUIDO, emergencia.getStatus());
        assertEquals("Estabilizado", emergencia.getObservacoes());
        assertThrows(RegraNegocioException.class, () -> emergencia.concluir(null));
    }

    @Test
    void emergenciaExigePrioridadeQueixaEMedicoAtivo() {
        assertThrows(RegraNegocioException.class,
                () -> new AtendimentoEmergencia(paciente, null, "Dor").validarRealizacao());
        assertThrows(RegraNegocioException.class,
                () -> new AtendimentoEmergencia(paciente, Prioridade.BAIXA, " ").validarRealizacao());
        assertThrows(RegraNegocioException.class, () -> emergencia().iniciar(null));
        clinico.desativar();
        assertThrows(RegraNegocioException.class, () -> emergencia().iniciar(clinico));
    }
}
