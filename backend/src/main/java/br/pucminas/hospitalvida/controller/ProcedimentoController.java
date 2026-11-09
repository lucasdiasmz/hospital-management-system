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

import br.pucminas.hospitalvida.dto.ObservacaoRequest;
import br.pucminas.hospitalvida.dto.ProcedimentoRequest;
import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;
import br.pucminas.hospitalvida.model.enums.StatusProcedimento;
import br.pucminas.hospitalvida.service.ProcedimentoService;

/** Fica dentro de /consultas porque a Sprint 2 pede apenas os cinco endpoints principais. */
@RestController
@RequestMapping("/consultas/procedimentos")
public class ProcedimentoController {

    private final ProcedimentoService service;

    public ProcedimentoController(ProcedimentoService service) {
        this.service = service;
    }

    /** Filtros opcionais: ?status=  ?cirurgiaoId=  ?pacienteId= */
    @GetMapping
    public List<ProcedimentoCirurgico> listar(
            @RequestParam(name = "status", required = false) StatusProcedimento status,
            @RequestParam(name = "cirurgiaoId", required = false) Long cirurgiaoId,
            @RequestParam(name = "pacienteId", required = false) Long pacienteId) {
        return service.listar(status, cirurgiaoId, pacienteId);
    }

    @GetMapping("/{id}")
    public ProcedimentoCirurgico buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ProcedimentoCirurgico> agendar(@RequestBody ProcedimentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.agendar(req));
    }

    @PatchMapping("/{id}/iniciar")
    public ProcedimentoCirurgico iniciar(@PathVariable("id") Long id) {
        return service.iniciar(id);
    }

    /** O corpo é opcional: {"texto": "observações pós-operatórias"} */
    @PatchMapping("/{id}/concluir")
    public ProcedimentoCirurgico concluir(@PathVariable("id") Long id,
                                          @RequestBody(required = false) ObservacaoRequest req) {
        return service.concluir(id, req == null ? null : req.texto());
    }

    @PatchMapping("/{id}/cancelar")
    public ProcedimentoCirurgico cancelar(@PathVariable("id") Long id) {
        return service.cancelar(id);
    }
}
