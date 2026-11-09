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

import br.pucminas.hospitalvida.dto.EmergenciaRequest;
import br.pucminas.hospitalvida.dto.InicioEmergenciaRequest;
import br.pucminas.hospitalvida.dto.ObservacaoRequest;
import br.pucminas.hospitalvida.model.AtendimentoEmergencia;
import br.pucminas.hospitalvida.model.enums.StatusEmergencia;
import br.pucminas.hospitalvida.service.EmergenciaService;

/** Fica dentro de /consultas porque a Sprint 2 pede apenas os cinco endpoints principais. */
@RestController
@RequestMapping("/consultas/emergencias")
public class EmergenciaController {

    private final EmergenciaService service;

    public EmergenciaController(EmergenciaService service) {
        this.service = service;
    }

    /** ?status=AGUARDANDO devolve a fila, da prioridade mais alta para a mais baixa. */
    @GetMapping
    public List<AtendimentoEmergencia> listar(
            @RequestParam(name = "status", required = false) StatusEmergencia status) {
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
    public AtendimentoEmergencia iniciar(@PathVariable("id") Long id, @RequestBody InicioEmergenciaRequest req) {
        return service.iniciar(id, req.profissionalId());
    }

    /** O corpo é opcional: {"texto": "observações finais"} */
    @PatchMapping("/{id}/concluir")
    public AtendimentoEmergencia concluir(@PathVariable("id") Long id,
                                          @RequestBody(required = false) ObservacaoRequest req) {
        return service.concluir(id, req == null ? null : req.texto());
    }
}
