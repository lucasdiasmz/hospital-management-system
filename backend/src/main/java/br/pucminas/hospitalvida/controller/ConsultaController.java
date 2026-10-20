package br.pucminas.hospitalvida.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospitalvida.dto.Requisicoes.ConsultaRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.MudancaSituacaoRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.ObservacaoRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.ReagendamentoRequest;
import br.pucminas.hospitalvida.model.Consulta;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.service.ConsultaService;

/**
 * Consultas de rotina e especializadas. Não há DELETE: consultas são canceladas
 * (mudança de situação), para não perder o histórico.
 */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public List<Consulta> listar(@RequestParam(name = "situacao", required = false) SituacaoConsulta situacao,
                                 @RequestParam(name = "pacienteId", required = false) Long pacienteId,
                                 @RequestParam(name = "profissionalId", required = false) Long profissionalId) {
        return service.listar(situacao, pacienteId, profissionalId);
    }

    @GetMapping("/{id}")
    public Consulta buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Consulta> agendar(@RequestBody ConsultaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agendar(req));
    }

    /** Body: {"situacao": "CONFIRMADA"} ou {"situacao": "CONCLUIDA", "observacoes": "..."} */
    @PatchMapping("/{id}/situacao")
    public Consulta alterarSituacao(@PathVariable("id") Long id, @RequestBody MudancaSituacaoRequest req) {
        return service.alterarSituacao(id, req);
    }

    @PatchMapping("/{id}/observacoes")
    public Consulta registrarObservacao(@PathVariable("id") Long id, @RequestBody ObservacaoRequest req) {
        return service.registrarObservacao(id, req);
    }

    /** Cria uma nova consulta a partir de uma CANCELADA ou NAO_COMPARECEU. */
    @PostMapping("/{id}/reagendamento")
    public ResponseEntity<Consulta> reagendar(@PathVariable("id") Long id, @RequestBody ReagendamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reagendar(id, req.novaDataHora()));
    }
}
