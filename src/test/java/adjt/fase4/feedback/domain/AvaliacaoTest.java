package adjt.fase4.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class AvaliacaoTest {

  private static final Instant AGORA = Instant.parse("2026-11-03T18:00:00Z");

  @Test
  void deveCriarAvaliacaoComIdentificadorDataEUrgencia() {
    Avaliacao avaliacao = Avaliacao.nova("  Áudio da aula cortando  ", 2, AGORA);

    assertEquals("Áudio da aula cortando", avaliacao.descricao());
    assertEquals(2, avaliacao.nota().valor());
    assertEquals(Urgencia.CRITICA, avaliacao.urgencia());
    assertTrue(avaliacao.critica());
    assertEquals(AGORA, avaliacao.dataEnvio());
    assertDoesNotThrow(() -> UUID.fromString(avaliacao.id()));
  }

  @Test
  void deveGerarIdentificadoresDiferentesParaCadaAvaliacao() {
    assertNotEquals(Avaliacao.nova("Aula boa", 8, AGORA).id(), Avaliacao.nova("Aula boa", 8, AGORA).id());
  }

  @Test
  void naoDeveSerCriticaQuandoANotaEstiverAcimaDoLimite() {
    assertFalse(Avaliacao.nova("Aula mediana", 4, AGORA).critica());
  }

  @Test
  void deveGuardarADataComPrecisaoDeMilissegundos() {
    Avaliacao avaliacao = Avaliacao.nova("Aula boa", 8, Instant.parse("2026-11-03T18:00:00.123456789Z"));

    assertEquals(Instant.parse("2026-11-03T18:00:00.123Z"), avaliacao.dataEnvio());
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = { "", "   " })
  void deveRejeitarDescricaoAusenteOuEmBranco(String descricao) {
    AvaliacaoInvalidaException erro = assertThrows(AvaliacaoInvalidaException.class,
        () -> Avaliacao.nova(descricao, 5, AGORA));

    assertEquals("O campo 'descricao' é obrigatório", erro.getMessage());
  }

  @Test
  void deveAceitarDescricaoNoTamanhoMaximo() {
    String descricao = "a".repeat(Avaliacao.TAMANHO_MAXIMO_DESCRICAO);

    assertEquals(descricao, Avaliacao.nova(descricao, 5, AGORA).descricao());
  }

  @Test
  void deveRejeitarDescricaoAcimaDoTamanhoMaximo() {
    String descricao = "a".repeat(Avaliacao.TAMANHO_MAXIMO_DESCRICAO + 1);

    AvaliacaoInvalidaException erro = assertThrows(AvaliacaoInvalidaException.class,
        () -> Avaliacao.nova(descricao, 5, AGORA));

    assertEquals("O campo 'descricao' deve ter no máximo 2000 caracteres", erro.getMessage());
  }

  @Test
  void deveRejeitarNotaAusente() {
    AvaliacaoInvalidaException erro = assertThrows(AvaliacaoInvalidaException.class,
        () -> Avaliacao.nova("Aula boa", null, AGORA));

    assertEquals("O campo 'nota' é obrigatório", erro.getMessage());
  }

  @Test
  void deveApontarPrimeiroADescricaoQuandoDescricaoENotaForemInvalidas() {
    AvaliacaoInvalidaException erro = assertThrows(AvaliacaoInvalidaException.class,
        () -> Avaliacao.nova(" ", 99, AGORA));

    assertEquals("O campo 'descricao' é obrigatório", erro.getMessage());
  }

  @Test
  void deveAplicarAsRegrasTambemAoReconstruirUmaAvaliacaoExistente() {
    assertThrows(AvaliacaoInvalidaException.class,
        () -> new Avaliacao("1", "", new Nota(5), AGORA));
    assertThrows(AvaliacaoInvalidaException.class,
        () -> new Avaliacao("1", "Aula boa", null, AGORA));
  }
}
