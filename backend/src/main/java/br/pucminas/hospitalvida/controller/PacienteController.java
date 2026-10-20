package br.pucminas.hospitalvida.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospitalvida.dto.HistoricoResponse;
import br.pucminas.hospitalvida.dto.Requisicoes.HistoricoRequest;
import br.pucminas.hospitalvida.dto.Requisicoes.PacienteRequest;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.service.PacienteService;

@RestController
@RequestMapping("/pacientes")
public class PacienteController {

    private final PacienteService service;

    public PacienteController(PacienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Paciente> listar(@RequestParam(name = "nome", required = false) String nome) {
        return service.listar(nome);
    }

    @GetMapping("/{id}")
    public Paciente buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Paciente> cadastrar(@RequestBody PacienteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(req));
    }

    @PutMapping("/{id}")
    public Paciente atualizar(@PathVariable("id") Long id, @RequestBody PacienteRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/historico")
    public HistoricoResponse historico(@PathVariable("id") Long id) {
        return service.historico(id);
    }

    @PutMapping("/{id}/historico")
    public HistoricoResponse atualizarHistorico(@PathVariable("id") Long id, @RequestBody HistoricoRequest req) {
        return service.atualizarInformacoesHistorico(id, req.informacoesRelevantes());
    }
}
