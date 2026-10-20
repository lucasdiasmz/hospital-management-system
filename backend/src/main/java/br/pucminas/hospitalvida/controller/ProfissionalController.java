package br.pucminas.hospitalvida.controller;

import java.time.LocalDateTime;
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

import br.pucminas.hospitalvida.dto.Requisicoes.ProfissionalRequest;
import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;
import br.pucminas.hospitalvida.model.ProfissionalSaude;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.service.ProfissionalService;

@RestController
@RequestMapping("/profissionais")
public class ProfissionalController {

    private final ProfissionalService service;

    public ProfissionalController(ProfissionalService service) {
        this.service = service;
    }

    /** Filtros opcionais: ?categoria=  ?especialidade=  ?disponivelEm=2026-10-20T10:00 */
    @GetMapping
    public List<ProfissionalSaude> listar(
            @RequestParam(name = "categoria", required = false) CategoriaProfissional categoria,
            @RequestParam(name = "especialidade", required = false) Especialidade especialidade,
            @RequestParam(name = "disponivelEm", required = false) LocalDateTime disponivelEm) {
        return service.listar(categoria, especialidade, disponivelEm);
    }

    @GetMapping("/{id}")
    public ProfissionalSaude buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<ProfissionalSaude> cadastrar(@RequestBody ProfissionalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(req));
    }

    @PutMapping("/{id}")
    public ProfissionalSaude atualizar(@PathVariable("id") Long id, @RequestBody ProfissionalRequest req) {
        return service.atualizar(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    /** Procedimentos registrados para um cirurgião (data, paciente, descrição...). */
    @GetMapping("/{id}/procedimentos")
    public List<ProcedimentoCirurgico> procedimentos(@PathVariable("id") Long id) {
        return service.procedimentos(id);
    }
}
