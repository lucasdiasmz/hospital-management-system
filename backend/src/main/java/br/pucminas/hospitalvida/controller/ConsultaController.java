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

import br.pucminas.hospitalvida.dto.ConsultaRequest;
import br.pucminas.hospitalvida.dto.ObservacaoRequest;
import br.pucminas.hospitalvida.dto.ReagendamentoRequest;
import br.pucminas.hospitalvida.dto.SituacaoRequest;
import br.pucminas.hospitalvida.model.Consulta;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.service.ConsultaService;

/** Não há DELETE: a consulta é cancelada pela troca de situação, para manter o histórico. */
@RestController
@RequestMapping("/consultas")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    /** Filtros opcionais: ?situacao=  ?pacienteId=  ?profissionalId= */
    @GetMapping
    public List<Consulta> listar(
            @RequestParam(name = "situacao", required = false) SituacaoConsulta situacao,
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

    @PatchMapping("/{id}/situacao")
    public Consulta alterarSituacao(@PathVariable("id") Long id, @RequestBody SituacaoRequest req) {
        return service.alterarSituacao(id, req);
    }

    @PatchMapping("/{id}/observacoes")
    public Consulta registrarObservacoes(@PathVariable("id") Long id, @RequestBody ObservacaoRequest req) {
        return service.registrarObservacoes(id, req.texto());
    }

    /** Cria um novo agendamento a partir de uma consulta CANCELADA ou NAO_COMPARECEU. */
    @PostMapping("/{id}/reagendamento")
    public ResponseEntity<Consulta> reagendar(@PathVariable("id") Long id, @RequestBody ReagendamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.reagendar(id, req));
    }
}
