package br.pucminas.hospitalvida.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospitalvida.dto.Requisicoes.QuartoRequest;
import br.pucminas.hospitalvida.model.Quarto;
import br.pucminas.hospitalvida.model.enums.SituacaoQuarto;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;
import br.pucminas.hospitalvida.service.QuartoService;

@RestController
@RequestMapping("/quartos")
public class QuartoController {

    private final QuartoService service;

    public QuartoController(QuartoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Quarto> listar(@RequestParam(name = "situacao", required = false) SituacaoQuarto situacao,
                               @RequestParam(name = "tipo", required = false) TipoQuarto tipo,
                               @RequestParam(name = "comVaga", required = false) Boolean comVaga) {
        return service.listar(situacao, tipo, comVaga);
    }

    @GetMapping("/{id}")
    public Quarto buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Quarto> cadastrar(@RequestBody QuartoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(req));
    }

    /** Bloqueia para manutenção (só se estiver vazio). */
    @PatchMapping("/{id}/bloquear")
    public Quarto bloquear(@PathVariable("id") Long id) {
        return service.bloquear(id);
    }

    @PatchMapping("/{id}/desbloquear")
    public Quarto desbloquear(@PathVariable("id") Long id) {
        return service.desbloquear(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
