package br.pucminas.hospitalvida.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.pucminas.hospitalvida.model.AtendimentoEmergencia;
import br.pucminas.hospitalvida.model.Atendimento;
import br.pucminas.hospitalvida.model.Cirurgiao;
import br.pucminas.hospitalvida.model.ClinicoGeral;
import br.pucminas.hospitalvida.model.ConsultaEspecializada;
import br.pucminas.hospitalvida.model.ConsultaRotina;
import br.pucminas.hospitalvida.model.Internacao;
import br.pucminas.hospitalvida.model.MedicoEspecialista;
import br.pucminas.hospitalvida.model.Paciente;
import br.pucminas.hospitalvida.model.ProcedimentoCirurgico;
import br.pucminas.hospitalvida.model.Quarto;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;
import br.pucminas.hospitalvida.repository.ConsultaRepository;
import br.pucminas.hospitalvida.repository.EmergenciaRepository;
import br.pucminas.hospitalvida.repository.InternacaoRepository;
import br.pucminas.hospitalvida.repository.PacienteRepository;
import br.pucminas.hospitalvida.repository.ProcedimentoRepository;
import br.pucminas.hospitalvida.repository.ProfissionalRepository;
import br.pucminas.hospitalvida.repository.QuartoRepository;

/**
 * Popula o banco H2 com dados de exemplo toda vez que a aplicação sobe,
 * para dar pra testar os endpoints sem cadastrar tudo na mão.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final PacienteRepository pacientes;
    private final ProfissionalRepository profissionais;
    private final QuartoRepository quartos;
    private final ConsultaRepository consultas;
    private final EmergenciaRepository emergencias;
    private final ProcedimentoRepository procedimentos;
    private final InternacaoRepository internacoes;

    public DataLoader(PacienteRepository pacientes, ProfissionalRepository profissionais, QuartoRepository quartos,
                      ConsultaRepository consultas, EmergenciaRepository emergencias,
                      ProcedimentoRepository procedimentos, InternacaoRepository internacoes) {
        this.pacientes = pacientes;
        this.profissionais = profissionais;
        this.quartos = quartos;
        this.consultas = consultas;
        this.emergencias = emergencias;
        this.procedimentos = procedimentos;
        this.internacoes = internacoes;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Paciente ana = pacientes.save(new Paciente("Ana Souza", "(31) 99999-0001", "ana@email.com",
                "123.456.789-00", LocalDate.of(1990, 5, 10), "Rua da Bahia, 100 - Centro, BH"));
        Paciente bruno = pacientes.save(new Paciente("Bruno Lima", "(31) 99999-0002", "bruno@email.com",
                "987.654.321-00", LocalDate.of(1978, 11, 2), "Av. Afonso Pena, 2000 - Funcionários, BH"));
        Paciente clara = pacientes.save(new Paciente("Clara Mendes", "(31) 99999-0003", null,
                "111.222.333-44", LocalDate.of(2005, 3, 21), "Rua Pernambuco, 50 - Savassi, BH"));

        ClinicoGeral carlos = profissionais.save(new ClinicoGeral("Dr. Carlos Silva", "CRM-MG 10001",
                "(31) 3333-0001", "carlos@hospitalvida.com"));
        MedicoEspecialista beatriz = profissionais.save(new MedicoEspecialista("Dra. Beatriz Rocha", "CRM-MG 20002",
                "(31) 3333-0002", "beatriz@hospitalvida.com", Especialidade.CARDIOLOGIA));
        profissionais.save(new MedicoEspecialista("Dr. Diego Alves", "CRM-MG 20003",
                "(31) 3333-0003", "diego@hospitalvida.com", Especialidade.ORTOPEDIA));
        Cirurgiao rafael = profissionais.save(new Cirurgiao("Dr. Rafael Costa", "CRM-MG 30004",
                "(31) 3333-0004", "rafael@hospitalvida.com", "Cirurgia geral"));

        Quarto enfermaria = quartos.save(new Quarto("101", 1, TipoQuarto.ENFERMARIA, 4));
        quartos.save(new Quarto("201", 2, TipoQuarto.APARTAMENTO, 1));
        quartos.save(new Quarto("UTI-01", 3, TipoQuarto.UTI, 6));
        quartos.save(new Quarto("ISO-01", 3, TipoQuarto.ISOLAMENTO, 1));

        // Próxima segunda-feira: sempre no futuro e dentro do horário de funcionamento
        LocalDateTime segunda9h = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atTime(9, 0);

        ConsultaRotina rotina = new ConsultaRotina(ana, carlos, segunda9h, "Check-up anual");
        consultas.save(registrar(rotina));

        ConsultaEspecializada cardio = carlos.encaminhar(ana, beatriz, Especialidade.CARDIOLOGIA,
                segunda9h.plusHours(2), "Avaliação de sopro cardíaco");
        consultas.save(registrar(cardio));

        AtendimentoEmergencia emergencia = new AtendimentoEmergencia(bruno, Prioridade.ALTA, "Dor abdominal intensa");
        emergencias.save(registrar(emergencia));

        ProcedimentoCirurgico apendicectomia = rafael.registrarProcedimento(bruno, "Apendicectomia videolaparoscópica",
                TipoCirurgia.ELETIVA, segunda9h.plusDays(1), 90, "Jejum de 8 horas");
        procedimentos.save(registrar(apendicectomia));

        Internacao internacao = new Internacao(clara, carlos, enfermaria, LocalDate.now().plusDays(3),
                "Observação por pneumonia leve");
        internacao.iniciar();
        clara.adicionarInternacao(internacao);
        internacoes.save(internacao);
    }

    /** Valida o atendimento e o liga ao paciente e ao profissional. */
    private <T extends Atendimento> T registrar(T atendimento) {
        atendimento.validar();
        atendimento.getPaciente().adicionarAtendimento(atendimento);
        if (atendimento.getProfissional() != null) {
            atendimento.getProfissional().adicionarAtendimento(atendimento);
        }
        return atendimento;
    }
}
