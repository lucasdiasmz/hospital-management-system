package br.pucminas.hospitalvida.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import br.pucminas.hospitalvida.model.enums.CategoriaProfissional;
import br.pucminas.hospitalvida.model.enums.Especialidade;
import br.pucminas.hospitalvida.model.enums.Prioridade;
import br.pucminas.hospitalvida.model.enums.SituacaoConsulta;
import br.pucminas.hospitalvida.model.enums.TipoCirurgia;
import br.pucminas.hospitalvida.model.enums.TipoConsulta;
import br.pucminas.hospitalvida.model.enums.TipoQuarto;

/**
 * Corpos (JSON) aceitos pelos endpoints. São records: classes imutáveis só com dados.
 * Campos do tipo enum só aceitam os valores definidos no enum; qualquer outro texto
 * é recusado com HTTP 400.
 */
public final class Requisicoes {

    private Requisicoes() {
    }

    public record PacienteRequest(String nome, String telefone, String email, String cpf,
                                  LocalDate dataNascimento, String endereco) {
    }

    public record HistoricoRequest(String informacoesRelevantes) {
    }

    /** especialidade só para ESPECIALISTA; areaCirurgica só para CIRURGIAO. */
    public record ProfissionalRequest(CategoriaProfissional categoria, String nome, String registroProfissional,
                                      String telefone, String email, Especialidade especialidade,
                                      String areaCirurgica) {
    }

    /** especialidade e encaminhadoPorId só para tipo ESPECIALIZADA. */
    public record ConsultaRequest(TipoConsulta tipo, Long pacienteId, Long profissionalId, LocalDateTime dataHora,
                                  String motivo, Especialidade especialidade, Long encaminhadoPorId) {
    }

    public record MudancaSituacaoRequest(SituacaoConsulta situacao, String observacoes) {
    }

    public record ObservacaoRequest(Long profissionalId, String texto) {
    }

    public record ReagendamentoRequest(LocalDateTime novaDataHora) {
    }

    public record EmergenciaRequest(Long pacienteId, Prioridade prioridade, String queixaPrincipal) {
    }

    public record IniciarAvaliacaoRequest(Long profissionalId) {
    }

    public record ConclusaoRequest(String observacoes) {
    }

    public record ProcedimentoRequest(Long pacienteId, Long cirurgiaoId, String descricao, TipoCirurgia tipoCirurgia,
                                      LocalDateTime dataHoraPrevista, Integer duracaoPrevistaMinutos,
                                      String observacoesPreOperatorias) {
    }

    public record InternacaoRequest(Long pacienteId, Long profissionalId, Long quartoId,
                                    LocalDate dataPrevistaAlta, String observacoes) {
    }

    public record AltaRequest(LocalDate dataAlta) {
    }

    public record TransferenciaRequest(Long quartoId) {
    }

    public record QuartoRequest(String numero, Integer andar, TipoQuarto tipo, Integer capacidadeMaxima) {
    }
}
