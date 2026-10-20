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

import br.pucminas.hospitalvida.dto.Requisicoes.AltaRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.InternacaoRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.TransferenciaRequest;
import br.pucminas.hospitalvida.model.Internacao;
import br.pucminas.hospitalvida.model.enums.StatusInternacao;
import br.pucminas.hospitalvida.service.InternacaoService;

@RestController
@RequestMapping("/internacoes")
public class InternacaoController {

    private final InternacaoService service;

    public InternacaoController(InternacaoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Internacao> listar(@RequestParam(name = "status", required = false) StatusInternacao status,
                                   @RequestParam(name = "pacienteId", required = false) Long pacienteId) {
        return service.listar(status, pacienteId);
    }

    @GetMapping("/{id}")
    public Internacao buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Internacao> internar(@RequestBody InternacaoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.internar(req));
    }

    /** Body opcional: {"dataAlta": "2026-10-25"}. Sem body, a alta é hoje. */
    @PatchMapping("/{id}/alta")
    public Internacao registrarAlta(@PathVariable("id") Long id, @RequestBody(required = false) AltaRequest req) {
        return service.registrarAlta(id, req == null ? null : req.dataAlta());
    }

    @PatchMapping("/{id}/transferencia")
    public Internacao transferir(@PathVariable("id") Long id, @RequestBody TransferenciaRequest req) {
        return service.transferir(id, req.quartoId());
    }

    @PatchMapping("/{id}/cancelar")
    public Internacao cancelar(@PathVariable("id") Long id) {
        return service.cancelar(id);
    }
}
