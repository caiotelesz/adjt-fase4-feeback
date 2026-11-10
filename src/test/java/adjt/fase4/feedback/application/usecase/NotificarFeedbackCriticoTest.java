package adjt.fase4.feedback.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import adjt.fase4.feedback.application.dto.NotificacaoCritica;

class NotificarFeedbackCriticoTest {

  private final List<String> assuntos = new ArrayList<>();
  private final List<String> corpos = new ArrayList<>();
  private final NotificarFeedbackCritico notificar = new NotificarFeedbackCritico(
      (assunto, corpo) -> {
        assuntos.add(assunto);
        corpos.add(corpo);
      },
      ZoneId.of("America/Sao_Paulo"));

  @Test
  void deveEnviarEmailComDescricaoUrgenciaEDataDeEnvioNoFusoDoAdministrador() {
    notificar.executar(new NotificacaoCritica("abc-123", "Áudio ruim", "CRITICA", "2026-11-03T18:00:00Z"));

    assertEquals(List.of("[FEEDBACK CRÍTICO] Novo feedback de aula"), assuntos);
    String corpo = corpos.get(0);
    assertTrue(corpo.contains("Descrição: Áudio ruim"));
    assertTrue(corpo.contains("Urgência: CRITICA"));
    assertTrue(corpo.contains("Data de envio: 03/11/2026 15:00:00 (America/Sao_Paulo)"));
    assertTrue(corpo.contains("Identificador: abc-123"));
  }

  @Test
  void deveFalharSemEnviarQuandoADataDaMensagemForInvalida() {
    assertThrows(DateTimeParseException.class,
        () -> notificar.executar(new NotificacaoCritica("1", "Áudio ruim", "CRITICA", "ontem")));

    assertTrue(assuntos.isEmpty());
  }
}
