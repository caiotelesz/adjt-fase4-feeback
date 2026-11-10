package adjt.fase4.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ResumoSemanalTest {

  private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

  private static Avaliacao avaliacao(String id, int nota, String dataEnvio) {
    return new Avaliacao(id, "Feedback " + id, new Nota(nota), Instant.parse(dataEnvio));
  }

  @Test
  void deveResumirListaVazia() {
    ResumoSemanal resumo = ResumoSemanal.de(List.of(), BRASILIA);

    assertTrue(resumo.vazio());
    assertEquals(0, resumo.total());
    assertEquals(0.0, resumo.mediaDasNotas());
    assertEquals(Map.of(Urgencia.CRITICA, 0L, Urgencia.ATENCAO, 0L, Urgencia.OK, 0L),
        resumo.quantidadePorUrgencia());
    assertTrue(resumo.quantidadePorDia().isEmpty());
  }

  @Test
  void deveCalcularTotalEMediaDasNotas() {
    ResumoSemanal resumo = ResumoSemanal.de(List.of(
        avaliacao("1", 9, "2026-11-03T12:00:00Z"),
        avaliacao("2", 2, "2026-11-03T13:00:00Z"),
        avaliacao("3", 5, "2026-11-05T12:00:00Z")), BRASILIA);

    assertFalse(resumo.vazio());
    assertEquals(3, resumo.total());
    assertEquals(16.0 / 3, resumo.mediaDasNotas(), 1e-9);
  }

  @Test
  void deveContarPorUrgenciaIncluindoAsQueNaoOcorreram() {
    ResumoSemanal resumo = ResumoSemanal.de(List.of(
        avaliacao("1", 9, "2026-11-03T12:00:00Z"),
        avaliacao("2", 10, "2026-11-03T13:00:00Z"),
        avaliacao("3", 1, "2026-11-05T12:00:00Z")), BRASILIA);

    assertEquals(Map.of(Urgencia.CRITICA, 1L, Urgencia.ATENCAO, 0L, Urgencia.OK, 2L),
        resumo.quantidadePorUrgencia());
  }

  @Test
  void deveContarPorDiaNoFusoInformado() {
    List<Avaliacao> avaliacoes = List.of(
        avaliacao("1", 9, "2026-11-03T02:30:00Z"),
        avaliacao("2", 2, "2026-11-03T12:00:00Z"));

    ResumoSemanal emBrasilia = ResumoSemanal.de(avaliacoes, BRASILIA);
    ResumoSemanal emUtc = ResumoSemanal.de(avaliacoes, ZoneOffset.UTC);

    assertEquals(Map.of(LocalDate.of(2026, 11, 2), 1L, LocalDate.of(2026, 11, 3), 1L),
        emBrasilia.quantidadePorDia());
    assertEquals(Map.of(LocalDate.of(2026, 11, 3), 2L), emUtc.quantidadePorDia());
  }

  @Test
  void deveListarAsAvaliacoesEmOrdemDeEnvio() {
    Avaliacao ultima = avaliacao("3", 5, "2026-11-05T12:00:00Z");
    Avaliacao primeira = avaliacao("1", 9, "2026-11-03T12:00:00Z");
    Avaliacao segunda = avaliacao("2", 2, "2026-11-03T13:00:00Z");

    ResumoSemanal resumo = ResumoSemanal.de(List.of(ultima, primeira, segunda), BRASILIA);

    assertEquals(List.of(primeira, segunda, ultima), resumo.avaliacoes());
  }
}
