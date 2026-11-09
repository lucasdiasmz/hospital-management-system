package br.pucminas.hospitalvida.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.pucminas.hospitalvida.dto.EncaminhamentoRequest;
import br.pucminas.hospitalvida.dto.ProfissionalRequest;
import br.pucminas.hospitalvida.model.Encaminhamento;
import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;
import br.pucminas.hospitalvida.model.Profissional;
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

    /** Filtros opcionais: ?categoria=  ?especialidade=  ?incluirInativos=true */
    @GetMapping
    public List<Profissional> listar(
            @RequestParam(name = "categoria", required = false) CategoriaProfissional categoria,
            @RequestParam(name = "especialidade", required = false) Especialidade especialidade,
            @RequestParam(name = "incluirInativos", defaultValue = "false") boolean incluirInativos) {
        return service.listar(categoria, especialidade, incluirInativos);
    }

    @GetMapping("/{id}")
    public Profissional buscar(@PathVariable("id") Long id) {
        return service.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Profissional> cadastrar(@RequestBody ProfissionalRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.cadastrar(req));
    }

    @PutMapping("/{id}")
    public Profissional atualizar(@PathVariable("id") Long id, @RequestBody ProfissionalRequest req) {
        return service.atualizar(id, req);
    }

    /** Em vez de DELETE: o profissional sai das buscas, mas o histórico dele é mantido. */
    @PatchMapping("/{id}/desativar")
    public Profissional desativar(@PathVariable("id") Long id) {
        return service.desativar(id);
    }

    @PatchMapping("/{id}/reativar")
    public Profissional reativar(@PathVariable("id") Long id) {
        return service.reativar(id);
    }

    /** O clínico geral {id} encaminha um paciente para uma especialidade. */
    @PostMapping("/{id}/encaminhamentos")
    public ResponseEntity<Encaminhamento> encaminhar(@PathVariable("id") Long id,
                                                     @RequestBody EncaminhamentoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.encaminhar(id, req));
    }

    @GetMapping("/{id}/encaminhamentos")
    public List<Encaminhamento> encaminhamentos(@PathVariable("id") Long id) {
        return service.encaminhamentosDoClinico(id);
    }

    @GetMapping("/{id}/procedimentos")
    public List<ProcedimentoCirurgico> procedimentos(@PathVariable("id") Long id) {
        return service.procedimentosDoCirurgiao(id);
    }
}
