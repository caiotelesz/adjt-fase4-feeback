package adjt.fase4.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NotaTest {

  @ParameterizedTest
  @ValueSource(ints = { 0, 5, 10 })
  void deveAceitarNotaDentroDoIntervalo(int valor) {
    assertEquals(valor, new Nota(valor).valor());
  }

  @ParameterizedTest
  @ValueSource(ints = { -1, 11 })
  void deveRejeitarNotaForaDoIntervalo(int valor) {
    AvaliacaoInvalidaException erro = assertThrows(AvaliacaoInvalidaException.class, () -> new Nota(valor));

    assertEquals("A nota deve estar entre 0 e 10", erro.getMessage());
  }

  @Test
  void deveRejeitarNotaNaoInformada() {
    AvaliacaoInvalidaException erro = assertThrows(AvaliacaoInvalidaException.class, () -> Nota.de(null));

    assertEquals("O campo 'nota' é obrigatório", erro.getMessage());
  }
}
