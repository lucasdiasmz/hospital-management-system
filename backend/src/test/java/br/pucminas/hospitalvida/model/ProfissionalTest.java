package br.pucminas.hospitalvida.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import br.pucminas.hospitalvida.exception.RegraNegocioException;
import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;

/** Regras dos profissionais de saúde, testadas sem subir o Spring. */
class ProfissionalTest {

    private final MedicoClinicoGeral clinico =
            new MedicoClinicoGeral("Dr. Carlos", "CRM-MG 1001", "3133330000", "carlos@hv.com");
    private final MedicoEspecialista cardiologista = new MedicoEspecialista("Dra. Beatriz", "CRM-MG 2002",
            "3133331111", "bia@hv.com", Especialidade.CARDIOLOGIA);
    private final Cirurgiao cirurgiao = new Cirurgiao("Dr. Rafael", "CRM-MG 3003", "3133332222",
            "rafael@hv.com", "Cirurgia geral", "RQE 12345");

    // ---------------- Categorias (polimorfismo) ----------------

    @Test
    void cadaCategoriaRespondeDoSeuJeito() {
        assertEquals(CategoriaProfissional.CLINICO_GERAL, clinico.getCategoria());
        assertEquals(CategoriaProfissional.ESPECIALISTA, cardiologista.getCategoria());
        assertEquals(CategoriaProfissional.CIRURGIAO, cirurgiao.getCategoria());

        assertTrue(clinico.podeRealizarConsultaRotina());
        assertFalse(cardiologista.podeRealizarConsultaRotina());
        assertFalse(cirurgiao.podeRealizarConsultaRotina());

        assertTrue(cirurgiao.podeRealizarCirurgia());
        assertFalse(clinico.podeRealizarCirurgia());
        assertFalse(cardiologista.podeRealizarCirurgia());
    }

    @Test
    void especialistaSoAtendeASuaEspecialidade() {
        assertTrue(cardiologista.atendeEspecialidade(Especialidade.CARDIOLOGIA));
        assertFalse(cardiologista.atendeEspecialidade(Especialidade.ORTOPEDIA));
        assertFalse(clinico.atendeEspecialidade(Especialidade.CARDIOLOGIA));
    }

    // ---------------- Dados obrigatórios ----------------

    @Test
    void especialistaPrecisaDeEspecialidade() {
        assertThrows(RegraNegocioException.class,
                () -> new MedicoEspecialista("Dr. X", "CRM 1", null, null, null));
    }

    @Test
    void cirurgiaoPrecisaDeAreaERqe() {
        assertThrows(RegraNegocioException.class,
                () -> new Cirurgiao("Dr. X", "CRM 1", null, null, " ", "RQE 1"));
        assertThrows(RegraNegocioException.class,
                () -> new Cirurgiao("Dr. X", "CRM 1", null, null, "Cirurgia geral", null));
    }

    @Test
    void nomeERegistroSaoObrigatorios() {
        assertThrows(RegraNegocioException.class, () -> new MedicoClinicoGeral("", "CRM 1", null, null));
        assertThrows(RegraNegocioException.class, () -> new MedicoClinicoGeral("Dr. X", null, null, null));
    }

    @Test
    void emailInvalidoERecusadoEOContatoNaoMuda() {
        assertThrows(RegraNegocioException.class,
                () -> new MedicoClinicoGeral("Dr. X", "CRM 1", null, "sem-arroba"));
        assertThrows(RegraNegocioException.class, () -> clinico.atualizarContato("999", "errado@"));
        assertEquals("carlos@hv.com", clinico.getEmail());
        assertEquals("3133330000", clinico.getTelefone());
    }

    // ---------------- Desativação ----------------

    @Test
    void profissionalDesativadoNaoRecebeAtendimentos() {
        assertTrue(cardiologista.isAtivo());
        cardiologista.desativar();
        assertFalse(cardiologista.isAtivo());
        assertThrows(RegraNegocioException.class, cardiologista::exigirAtivo);
        assertThrows(RegraNegocioException.class, cardiologista::desativar);
        cardiologista.reativar();
        cardiologista.exigirAtivo();
    }

    // ---------------- Encaminhamento ----------------

    @Test
    void clinicoEncaminhaParaUmaEspecialidade() {
        Paciente paciente = paciente();
        Encaminhamento encaminhamento =
                clinico.encaminhar(paciente, Especialidade.CARDIOLOGIA, null, "Sopro no coração");
        assertEquals(Especialidade.CARDIOLOGIA, encaminhamento.getEspecialidade());
        assertTrue(encaminhamento.getClinico() == clinico);
        assertTrue(encaminhamento.getPaciente() == paciente);
        assertNull(encaminhamento.getEspecialistaIndicado());
    }

    @Test
    void especialistaIndicadoPrecisaSerDaEspecialidade() {
        assertThrows(RegraNegocioException.class,
                () -> clinico.encaminhar(paciente(), Especialidade.ORTOPEDIA, cardiologista, "Dor no joelho"));
        assertThrows(RegraNegocioException.class,
                () -> clinico.encaminhar(paciente(), Especialidade.CARDIOLOGIA, cirurgiao, "Sopro"));
        Encaminhamento ok = clinico.encaminhar(paciente(), Especialidade.CARDIOLOGIA, cardiologista, "Sopro");
        assertTrue(ok.getEspecialistaIndicado() == cardiologista);
    }

    @Test
    void encaminhamentoExigeMotivoEPaciente() {
        assertThrows(RegraNegocioException.class,
                () -> clinico.encaminhar(paciente(), Especialidade.CARDIOLOGIA, null, " "));
        assertThrows(RegraNegocioException.class,
                () -> clinico.encaminhar(null, Especialidade.CARDIOLOGIA, null, "Sopro"));
    }

    @Test
    void clinicoOuEspecialistaDesativadoNaoEncaminha() {
        cardiologista.desativar();
        assertThrows(RegraNegocioException.class,
                () -> clinico.encaminhar(paciente(), Especialidade.CARDIOLOGIA, cardiologista, "Sopro"));
        clinico.desativar();
        assertThrows(RegraNegocioException.class,
                () -> clinico.encaminhar(paciente(), Especialidade.CARDIOLOGIA, null, "Sopro"));
    }

    /**
     * ATENÇÃO: ajuste esta linha para o construtor do Paciente que a Pessoa 1 criar.
     */
    private Paciente paciente() {
        return new Paciente();
    }
}
