package adjt.fase4.feedback.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.Nota;
import adjt.fase4.feedback.domain.ResumoSemanal;

class GerarRelatorioSemanalTest {

  private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");
  private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);
  private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

  private final List<Instant> periodosConsultados = new ArrayList<>();
  private final List<String> assuntos = new ArrayList<>();
  private final List<String> corpos = new ArrayList<>();

  private GerarRelatorioSemanal casoDeUsoCom(List<Avaliacao> avaliacoes) {
    return new GerarRelatorioSemanal(
        (inicio, fim) -> {
          periodosConsultados.add(inicio);
          periodosConsultados.add(fim);
          return avaliacoes;
        },
        (assunto, corpo) -> {
          assuntos.add(assunto);
          corpos.add(corpo);
        },
        RELOGIO, BRASILIA);
  }

  private static Avaliacao avaliacao(String descricao, int nota, String dataEnvio) {
    return new Avaliacao(descricao, descricao, new Nota(nota), Instant.parse(dataEnvio));
  }

  @Test
  void deveConsultarOsUltimosSeteDias() {
    casoDeUsoCom(List.of()).executar();

    assertEquals(List.of(Instant.parse("2026-11-02T12:00:00.001Z"), AGORA), periodosConsultados);
  }

  @Test
  void deveEncaixarOsPeriodosDeDuasSemanasSemIntervaloNemSobreposicao() {
    GerarRelatorioSemanal casoDeUso = casoDeUsoCom(List.of());
    Instant primeiraSegunda = Instant.parse("2026-11-09T12:00:00Z");
    Instant segundaSeguinte = Instant.parse("2026-11-16T12:00:00Z");

    casoDeUso.executar(primeiraSegunda);
    casoDeUso.executar(segundaSeguinte);

    Instant fimDoPrimeiro = periodosConsultados.get(1);
    Instant inicioDoSegundo = periodosConsultados.get(2);
    assertEquals(primeiraSegunda, fimDoPrimeiro);
    assertEquals(fimDoPrimeiro.plusMillis(1), inicioDoSegundo);
  }

  @Test
  void deveEnviarORelatorioComTodosOsDadosPedidos() {
    ResumoSemanal resumo = casoDeUsoCom(List.of(
        avaliacao("Conteúdo mediano", 5, "2026-11-05T12:00:00Z"),
        avaliacao("Aula ótima", 9, "2026-11-03T13:30:00Z"),
        avaliacao("Áudio ruim", 2, "2026-11-03T21:00:00Z"))).executar();

    assertEquals(3, resumo.total());
    assertEquals(List.of("Relatório semanal de feedbacks"), assuntos);
    assertEquals("""
        RELATÓRIO SEMANAL DE FEEDBACKS
        Período: 02/11/2026 a 09/11/2026 (America/Sao_Paulo)
        Total de avaliações: 3
        Média das notas: 5,33

        QUANTIDADE POR URGÊNCIA
        - CRITICA: 1
        - ATENCAO: 1
        - OK: 1

        QUANTIDADE POR DIA
        - 03/11/2026: 2
        - 05/11/2026: 1

        AVALIAÇÕES
        - [OK] 03/11/2026 10:30:00 | nota 9 | Aula ótima
        - [CRITICA] 03/11/2026 18:00:00 | nota 2 | Áudio ruim
        - [ATENCAO] 05/11/2026 09:00:00 | nota 5 | Conteúdo mediano
        """, corpos.get(0));
  }

  @Test
  void deveListarApenasAsAvaliacoesMaisRecentesQuandoHouverMuitas() {
    List<Avaliacao> muitas = new ArrayList<>();
    for (int i = 0; i < 205; i++) {
      muitas.add(new Avaliacao("id-" + i, "Feedback numero " + i, new Nota(8),
          Instant.parse("2026-11-03T12:00:00Z").plusSeconds(i)));
    }

    casoDeUsoCom(muitas).executar();

    String corpo = corpos.get(0);
    assertTrue(corpo.contains("Total de avaliações: 205"));
    assertTrue(corpo.contains("AVALIAÇÕES (as 200 mais recentes de 205)"));
    assertFalse(corpo.contains("| Feedback numero 4\n"));
    assertTrue(corpo.contains("| Feedback numero 5\n"));
    assertTrue(corpo.contains("| Feedback numero 204\n"));
  }

  @Test
  void deveResumirDescricoesLongasNaListagem() {
    String longa = "a".repeat(250);

    casoDeUsoCom(List.of(avaliacao(longa, 8, "2026-11-03T12:00:00Z"))).executar();

    assertTrue(corpos.get(0).contains("| " + "a".repeat(200) + "…\n"));
    assertFalse(corpos.get(0).contains("a".repeat(201)));
  }

  @Test
  void deveEnviarORelatorioMesmoSemAvaliacoesNoPeriodo() {
    casoDeUsoCom(List.of()).executar();

    assertEquals(1, corpos.size());
    assertTrue(corpos.get(0).contains("Total de avaliações: 0"));
    assertTrue(corpos.get(0).contains("Nenhuma avaliação recebida no período."));
    assertFalse(corpos.get(0).contains("Média das notas"));
  }

  @Test
  void devePropagarAFalhaDoEnvioParaQueAExecucaoConteComoErro() {
    GerarRelatorioSemanal comFalha = new GerarRelatorioSemanal(
        (inicio, fim) -> List.of(),
        (assunto, corpo) -> {
          throw new IllegalStateException("SES indisponível");
        },
        RELOGIO, BRASILIA);

    assertThrows(IllegalStateException.class, comFalha::executar);
  }
}
