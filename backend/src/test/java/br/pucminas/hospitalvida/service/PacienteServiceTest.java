package br.pucminas.hospitalvida.service;

import br.pucminas.hospitalvida.exception.RecursoNaoEncontradoException;
import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.Paciente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class PacienteServiceTest {

    @Autowired
    PacienteService service;

    private Paciente paciente(String nome, String cpf) {
        Paciente p = new Paciente();
        p.setNome(nome);
        p.setCpf(cpf);
        p.setDataNascimento(LocalDate.of(1990, 5, 10));
        p.setTelefone("(31) 99999-0000");
        p.setEmail("teste@hospitalvida.com");
        p.setEndereco("Rua A, 100");
        return p;
    }

    @Test
    void crudCompletoComCpfNormalizadoEUnico() {
        Paciente ana = service.cadastrar(paciente("Ana", "529.982.247-25"));
        assertEquals("52998224725", ana.getCpf());

        // mesmo CPF, com ou sem máscara, não pode cadastrar de novo
        assertThrows(RegraNegocioException.class, () -> service.cadastrar(paciente("Outra", "52998224725")));

        Paciente bruno = service.cadastrar(paciente("Bruno", "111.444.777-35"));
        // atualizar mantendo o próprio CPF é permitido; roubar o CPF de outro não
        assertEquals("Ana Maria", service.atualizar(ana.getId(), paciente("Ana Maria", "52998224725")).getNome());
        assertThrows(RegraNegocioException.class, () -> service.atualizar(bruno.getId(), paciente("Bruno", "52998224725")));

        service.remover(bruno.getId());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorId(bruno.getId()));
        assertThrows(RecursoNaoEncontradoException.class, () -> service.atualizar(999L, paciente("X", "52998224725")));
    }
}
