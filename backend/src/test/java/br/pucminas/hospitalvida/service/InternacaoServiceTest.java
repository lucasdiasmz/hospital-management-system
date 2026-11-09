package br.pucminas.hospitalvida.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.dto.InternacaoRequest;
import br.pucminas.hospitalvida.dto.ProcedimentoRequest;
import br.pucminas.hospitalvida.dto.ProfissionalRequest;
import br.pucminas.hospitalvida.dto.QuartoRequest;
import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.Internacao;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.Quarto;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;

/** Integração das internações, quartos e cirurgias com pacientes e profissionais, usando o banco H2. */
@SpringBootTest
@Transactional
class InternacaoServiceTest {

    @Autowired
    InternacaoService internacaoService;

    @Autowired
    QuartoService quartoService;

    @Autowired
    ProcedimentoService procedimentoService;

    @Autowired
    PacienteService pacienteService;

    @Autowired
    ProfissionalService profissionalService;

    private Long pacienteId;
    private Long clinicoId;
    private Long cirurgiaoId;

    @BeforeEach
    void cadastrarPessoas() {
        Paciente paciente = new Paciente();
        paciente.setNome("Teste Internação");
        paciente.setCpf("111.444.777-35");
        paciente.setDataNascimento(LocalDate.of(1990, 5, 10));
        paciente.setTelefone("(31) 99999-0000");
        paciente.setEmail("internacao@hospitalvida.com");
        paciente.setEndereco("Rua A, 100");
        pacienteId = pacienteService.cadastrar(paciente).getId();

        clinicoId = profissionalService.cadastrar(new ProfissionalRequest(CategoriaProfissional.CLINICO_GERAL,
                "Dr. Carlos", "CRM INT 1", null, null, null, null, null)).getId();
        cirurgiaoId = profissionalService.cadastrar(new ProfissionalRequest(CategoriaProfissional.CIRURGIAO,
                "Dr. Rafael", "CRM INT 2", null, null, null, "Cirurgia geral", "RQE 1")).getId();
    }

    private Quarto quarto(String numero, TipoQuarto tipo, int capacidade) {
        return quartoService.cadastrar(new QuartoRequest(numero, 1, tipo, capacidade));
    }

    private InternacaoRequest internar(Long quartoId) {
        return new InternacaoRequest(pacienteId, clinicoId, quartoId, null, "Observação");
    }

    @Test
    void pacienteNaoPodeTerDuasInternacoesAtivas() {
        Quarto quarto = quarto("T-101", TipoQuarto.ENFERMARIA, 4);
        Internacao internacao = internacaoService.internar(internar(quarto.getId()));
        assertThrows(RegraNegocioException.class, () -> internacaoService.internar(internar(quarto.getId())));

        internacaoService.registrarAlta(internacao.getId(), null);
        assertEquals(StatusInternacao.ENCERRADA, internacaoService.buscar(internacao.getId()).getStatus());
        internacaoService.internar(internar(quarto.getId()));
        assertEquals(1, quartoService.buscar(quarto.getId()).getOcupacaoAtual());
    }

    @Test
    void transferenciaAtualizaOsDoisQuartos() {
        Quarto origem = quarto("T-102", TipoQuarto.ENFERMARIA, 1);
        Quarto destino = quarto("T-103", TipoQuarto.APARTAMENTO, 1);
        Internacao internacao = internacaoService.internar(internar(origem.getId()));
        assertEquals(SituacaoQuarto.LOTADO, quartoService.buscar(origem.getId()).getSituacao());

        internacaoService.transferir(internacao.getId(), destino.getId());
        assertEquals(SituacaoQuarto.DISPONIVEL, quartoService.buscar(origem.getId()).getSituacao());
        assertEquals(SituacaoQuarto.LOTADO, quartoService.buscar(destino.getId()).getSituacao());
    }

    @Test
    void quartoNaoDuplicaNumeroEComHistoricoNaoEExcluido() {
        Quarto quarto = quarto("T-104", TipoQuarto.ENFERMARIA, 2);
        assertThrows(RegraNegocioException.class, () -> quarto("T-104", TipoQuarto.UTI, 2));

        Internacao internacao = internacaoService.internar(internar(quarto.getId()));
        internacaoService.cancelar(internacao.getId());
        assertThrows(RegraNegocioException.class, () -> quartoService.excluir(quarto.getId()));

        Quarto vazio = quarto("T-105", TipoQuarto.UTI, 2);
        quartoService.excluir(vazio.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> quartoService.buscar(vazio.getId()));
    }

    @Test
    void filtraQuartosComVagaEPorTipo() {
        quarto("T-106", TipoQuarto.ISOLAMENTO, 1);
        Quarto lotado = quarto("T-107", TipoQuarto.ISOLAMENTO, 1);
        internacaoService.internar(internar(lotado.getId()));
        assertEquals(1, quartoService.listar(null, TipoQuarto.ISOLAMENTO, true).size());
        assertEquals(1, quartoService.listar(SituacaoQuarto.LOTADO, TipoQuarto.ISOLAMENTO, null).size());
    }

    @Test
    void procedimentoApareceNaListaDoCirurgiaoEExigeCirurgiao() {
        ProcedimentoRequest req = new ProcedimentoRequest(LocalDate.now().plusDays(1), LocalTime.of(8, 0), pacienteId,
                cirurgiaoId, "Apendicectomia", TipoCirurgia.ELETIVA, "Jejum");
        Long id = procedimentoService.agendar(req).getId();

        assertEquals(id, profissionalService.procedimentosDoCirurgiao(cirurgiaoId).get(0).getId());
        assertThrows(RegraNegocioException.class, () -> profissionalService.procedimentosDoCirurgiao(clinicoId));
        assertThrows(RegraNegocioException.class, () -> procedimentoService.agendar(new ProcedimentoRequest(
                req.data(), req.horario(), pacienteId, clinicoId, "Apendicectomia", TipoCirurgia.ELETIVA, null)));
        assertThrows(RegraNegocioException.class, () -> procedimentoService.concluir(id, null));
    }
}
