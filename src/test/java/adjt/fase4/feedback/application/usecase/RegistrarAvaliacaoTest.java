package adjt.fase4.feedback.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.AvaliacaoInvalidaException;

class RegistrarAvaliacaoTest {

  private static final Instant AGORA = Instant.parse("2026-11-03T18:00:00Z");
  private static final Clock RELOGIO = Clock.fixed(AGORA, ZoneOffset.UTC);

  private final List<Avaliacao> salvas = new ArrayList<>();
  private final List<Avaliacao> alertas = new ArrayList<>();
  private final RegistrarAvaliacao registrar = new RegistrarAvaliacao(salvas::add, alertas::add, RELOGIO);

  @Test
  void deveGravarAAvaliacaoComADataDoRelogio() {
    RegistrarAvaliacao.Resultado resultado = registrar.executar("Aula ótima", 9);

    assertEquals(List.of(resultado.avaliacao()), salvas);
    assertEquals(AGORA, resultado.avaliacao().dataEnvio());
    assertFalse(resultado.alertaNaoPublicado());
  }

  @ParameterizedTest
  @ValueSource(ints = { 0, 3 })
  void devePublicarAlertaQuandoAAvaliacaoForCritica(int nota) {
    RegistrarAvaliacao.Resultado resultado = registrar.executar("Áudio ruim", nota);

    assertEquals(List.of(resultado.avaliacao()), alertas);
    assertFalse(resultado.alertaNaoPublicado());
  }

  @ParameterizedTest
  @ValueSource(ints = { 4, 6, 7, 10 })
  void naoDevePublicarAlertaQuandoAAvaliacaoNaoForCritica(int nota) {
    registrar.executar("Aula", nota);

    assertEquals(1, salvas.size());
    assertTrue(alertas.isEmpty());
  }

  @Test
  void naoDeveGravarNemAlertarQuandoOsDadosForemInvalidos() {
    assertThrows(AvaliacaoInvalidaException.class, () -> registrar.executar("Aula", 11));

    assertTrue(salvas.isEmpty());
    assertTrue(alertas.isEmpty());
  }

  @Test
  void deveManterAAvaliacaoGravadaEInformarAFalhaQuandoOAlertaNaoForPublicado() {
    IllegalStateException falha = new IllegalStateException("SNS indisponível");
    RegistrarAvaliacao comFalhaNoAlerta = new RegistrarAvaliacao(salvas::add, avaliacao -> {
      throw falha;
    }, RELOGIO);

    RegistrarAvaliacao.Resultado resultado = comFalhaNoAlerta.executar("Áudio ruim", 1);

    assertEquals(List.of(resultado.avaliacao()), salvas);
    assertTrue(resultado.alertaNaoPublicado());
    assertSame(falha, resultado.falhaNoAlerta());
  }

  @Test
  void naoDevePublicarAlertaQuandoAGravacaoFalhar() {
    RegistrarAvaliacao comFalhaNaGravacao = new RegistrarAvaliacao(avaliacao -> {
      throw new IllegalStateException("DynamoDB indisponível");
    }, alertas::add, RELOGIO);

    assertThrows(IllegalStateException.class, () -> comFalhaNaGravacao.executar("Áudio ruim", 1));

    assertTrue(alertas.isEmpty());
  }
}
