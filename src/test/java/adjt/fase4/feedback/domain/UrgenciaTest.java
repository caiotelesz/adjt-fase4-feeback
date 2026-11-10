package adjt.fase4.feedback.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UrgenciaTest {

  @ParameterizedTest
  @CsvSource({
      "0, CRITICA", "3, CRITICA",
      "4, ATENCAO", "6, ATENCAO",
      "7, OK", "10, OK"
  })
  void deveClassificarNosLimitesDeCadaFaixa(int nota, Urgencia esperada) {
    assertEquals(esperada, Urgencia.de(new Nota(nota)));
  }
}
