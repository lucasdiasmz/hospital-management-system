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

import br.pucminas.hospitalvida.dto.Requisicoes.ConclusaoRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.EmergenciaRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.IniciarAvaliacaoRequest;
import br.pucminas.hospitalvida.model.AtendimentoEmergencia;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import br.pucminas.hospitalvida.service.EmergenciaService;

/** Endpoint extra (além dos 5 pedidos) para a modalidade de emergência. */
@RestController
@RequestMapping("/emergencias")
public class EmergenciaController {

    private final EmergenciaService service;

    public EmergenciaController(EmergenciaService service) {
        this.service = service;
    }

    /** ?status=AGUARDANDO_AVALIACAO devolve a fila ordenada por prioridade. */
    @GetMapping
    public List<AtendimentoEmergencia> listar(@RequestParam(name = "status", required = false) StatusEmergencia status) {
        return service.listar(status);
    }

    @GetMapping("/{id}")
    public AtendimentoEmergencia buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<AtendimentoEmergencia> registrar(@RequestBody EmergenciaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(req));
    }

    @PatchMapping("/{id}/iniciar")
    public AtendimentoEmergencia iniciar(@PathVariable("id") Long id, @RequestBody IniciarAvaliacaoRequest req) {
        return service.iniciarAvaliacao(id, req.profissionalId());
    }

    @PatchMapping("/{id}/concluir")
    public AtendimentoEmergencia concluir(@PathVariable("id") Long id, @RequestBody(required = false) ConclusaoRequest req) {
        return service.concluir(id, req == null ? null : req.observacoes());
    }
}
