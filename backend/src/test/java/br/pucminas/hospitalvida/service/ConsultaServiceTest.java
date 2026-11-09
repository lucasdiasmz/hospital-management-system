package br.pucminas.hospitalvida.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.ConsultaRequest;
import br.pucminas.hospitalvida.dto.EmergenciaRequest;
import br.pucminas.hospitalvida.dto.EncaminhamentoRequest;
import br.pucminas.hospitalvida.dto.ProfissionalRequest;
import br.pucminas.hospitalvida.dto.ReagendamentoRequest;
import br.pucminas.hospitalvida.dto.SituacaoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.AtendimentoEmergencia;
import br.pucminas.hospitalvida.model.Consulta;
import br.pucminas.hospitalvida.model.ConsultaEspecializada;
import br.pucminas.hospitalvida.model.Encaminhamento;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import br.pucminas.hospitalvida.model.enums.TipoConsulta;

/** Integração com pacientes (Pessoa 1) e profissionais (Pessoa 2), usando o banco H2. */
@SpringBootTest
@Transactional
class ConsultaServiceTest {

    private static final LocalDate AMANHA = LocalDate.now().plusDays(1);
    private static final LocalTime DEZ_HORAS = LocalTime.of(10, 0);

    @Autowired
    ConsultaService service;

    @Autowired
    EmergenciaService emergenciaService;

    @Autowired
    PacienteService pacienteService;

    @Autowired
    ProfissionalService profissionalService;

    private Long pacienteId;
    private Long clinicoId;
    private Long cardiologistaId;

    @BeforeEach
    void cadastrarPessoas() {
        Paciente paciente = new Paciente();
        paciente.setNome("Teste Consulta");
        paciente.setCpf("123.456.789-09");
        paciente.setDataNascimento(LocalDate.of(1990, 5, 10));
        paciente.setTelefone("(31) 99999-0000");
        paciente.setEmail("consulta@hospitalvida.com");
        paciente.setEndereco("Rua A, 100");
        pacienteId = pacienteService.cadastrar(paciente).getId();

        clinicoId = profissionalService.cadastrar(new ProfissionalRequest(CategoriaProfissional.CLINICO_GERAL,
                "Dr. Carlos", "CRM TESTE 1", null, null, null, null, null)).getId();
        cardiologistaId = profissionalService.cadastrar(new ProfissionalRequest(CategoriaProfissional.ESPECIALISTA,
                "Dra. Beatriz", "CRM TESTE 2", null, null, Especialidade.CARDIOLOGIA, null, null)).getId();
    }

    private ConsultaRequest rotina() {
        return new ConsultaRequest(TipoConsulta.ROTINA, AMANHA, DEZ_HORAS, pacienteId, clinicoId, "Check-up", null, null, null);
    }

    private ConsultaRequest cardiologia(Long profissionalId, Long encaminhamentoId) {
        return new ConsultaRequest(TipoConsulta.ESPECIALIZADA, AMANHA, DEZ_HORAS, pacienteId, profissionalId, "Sopro",
                null, Especialidade.CARDIOLOGIA, encaminhamentoId);
    }

    private SituacaoRequest situacao(SituacaoConsulta situacao) {
        return new SituacaoRequest(situacao, null);
    }

    @Test
    void agendaEFiltraConsultas() {
        Consulta consulta = service.agendar(rotina());
        assertEquals(SituacaoConsulta.AGENDADA, consulta.getSituacao());
        assertEquals("ROTINA", consulta.getTipo());

        assertEquals(1, service.listar(SituacaoConsulta.AGENDADA, pacienteId, clinicoId).size());
        assertEquals(0, service.listar(SituacaoConsulta.CONCLUIDA, pacienteId, clinicoId).size());
        assertEquals(0, service.listar(null, pacienteId, cardiologistaId).size());
    }

    @Test
    void recusaProfissionalIncompativelOuInexistente() {
        assertThrows(RegraNegocioException.class, () -> service.agendar(cardiologia(clinicoId, null)));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.agendar(cardiologia(999999L, null)));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.agendar(cardiologia(cardiologistaId, 999999L)));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(999999L));
    }

    @Test
    void consultaEspecializadaUsaOEncaminhamentoDoClinico() {
        Encaminhamento encaminhamento = profissionalService.encaminhar(clinicoId,
                new EncaminhamentoRequest(pacienteId, Especialidade.CARDIOLOGIA, null, "Sopro"));
        ConsultaEspecializada consulta =
                (ConsultaEspecializada) service.agendar(cardiologia(cardiologistaId, encaminhamento.getId()));
        assertEquals(encaminhamento.getId(), consulta.getEncaminhamentoId());
    }

    @Test
    void trocaSituacaoRespeitandoAsTransicoes() {
        Long id = service.agendar(rotina()).getId();
        service.alterarSituacao(id, situacao(SituacaoConsulta.CONFIRMADA));
        service.alterarSituacao(id, situacao(SituacaoConsulta.EM_ATENDIMENTO));
        Consulta concluida = service.alterarSituacao(id,
                new SituacaoRequest(SituacaoConsulta.CONCLUIDA, "Paciente saudável"));
        assertEquals(SituacaoConsulta.CONCLUIDA, concluida.getSituacao());
        assertEquals("Paciente saudável", concluida.getObservacoes());
        assertThrows(RegraNegocioException.class, () -> service.alterarSituacao(id, situacao(SituacaoConsulta.AGENDADA)));
    }

    @Test
    void reagendaUmaVezSo() {
        Long id = service.agendar(rotina()).getId();
        service.alterarSituacao(id, situacao(SituacaoConsulta.CANCELADA));
        ReagendamentoRequest req = new ReagendamentoRequest(AMANHA.plusDays(1), DEZ_HORAS);

        Consulta nova = service.reagendar(id, req);
        assertEquals(id, nova.getConsultaOriginalId());
        assertEquals(SituacaoConsulta.AGENDADA, nova.getSituacao());
        assertThrows(RegraNegocioException.class, () -> service.reagendar(id, req));
    }

    @Test
    void filaDeEmergenciaComecaPelaMaiorPrioridade() {
        emergenciaService.registrar(new EmergenciaRequest(pacienteId, Prioridade.BAIXA, "Corte leve"));
        AtendimentoEmergencia critica =
                emergenciaService.registrar(new EmergenciaRequest(pacienteId, Prioridade.CRITICA, "Parada"));

        List<AtendimentoEmergencia> fila = emergenciaService.listar(StatusEmergencia.AGUARDANDO);
        assertEquals(Prioridade.CRITICA, fila.get(0).getPrioridade());

        emergenciaService.iniciar(critica.getId(), clinicoId);
        assertEquals(1, emergenciaService.listar(StatusEmergencia.INICIADO).size());
        assertEquals(StatusEmergencia.CONCLUIDO, emergenciaService.concluir(critica.getId(), null).getStatus());
    }
}
