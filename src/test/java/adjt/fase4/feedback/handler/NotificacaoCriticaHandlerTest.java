package adjt.fase4.feedback.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;

import adjt.fase4.feedback.application.port.EnviadorEmail;
import adjt.fase4.feedback.application.usecase.NotificarFeedbackCritico;

class NotificacaoCriticaHandlerTest {

  private static final String MENSAGEM = "{\"id\":\"abc-123\",\"descricao\":\"Áudio ruim\","
      + "\"urgencia\":\"CRITICA\",\"dataEnvio\":\"2026-11-03T18:00:00Z\"}";

  private final List<String> corpos = new ArrayList<>();
  private final ContextoFalso contexto = new ContextoFalso();
  private final NotificacaoCriticaHandler handler = handlerCom((assunto, corpo) -> corpos.add(corpo));

  private static NotificacaoCriticaHandler handlerCom(EnviadorEmail enviadorEmail) {
    return new NotificacaoCriticaHandler(
        new NotificarFeedbackCritico(enviadorEmail, ZoneId.of("America/Sao_Paulo")));
  }

  private static SNSEvent eventoCom(String... mensagens) {
    List<SNSEvent.SNSRecord> registros = new ArrayList<>();
    for (String mensagem : mensagens) {
      SNSEvent.SNSRecord registro = new SNSEvent.SNSRecord();
      registro.setSns(new SNSEvent.SNS().withMessage(mensagem));
      registros.add(registro);
    }
    SNSEvent evento = new SNSEvent();
    evento.setRecords(registros);
    return evento;
  }

  @Test
  void deveEnviarOEmailComOsDadosDaMensagemDoTopico() {
    handler.handleRequest(eventoCom(MENSAGEM), contexto);

    assertEquals(1, corpos.size());
    assertTrue(corpos.get(0).contains("Descrição: Áudio ruim"));
    assertTrue(corpos.get(0).contains("Urgência: CRITICA"));
    assertTrue(corpos.get(0).contains("Data de envio: 03/11/2026 15:00:00"));
    assertEquals(List.of("Alerta crítico enviado por e-mail id=abc-123"), contexto.logs);
  }

  @Test
  void deveEnviarUmEmailPorMensagemRecebida() {
    handler.handleRequest(eventoCom(MENSAGEM, MENSAGEM), contexto);

    assertEquals(2, corpos.size());
  }

  @Test
  void deveFalharSemEnviarQuandoAMensagemNaoForJsonValido() {
    assertThrows(IllegalStateException.class,
        () -> handler.handleRequest(eventoCom("isto não é JSON"), contexto));

    assertTrue(corpos.isEmpty());
  }

  @Test
  void devePropagarAFalhaDoEnvioParaQueOLambdaTenteNovamente() {
    NotificacaoCriticaHandler comFalha = handlerCom((assunto, corpo) -> {
      throw new IllegalStateException("SES indisponível");
    });

    assertThrows(IllegalStateException.class, () -> comFalha.handleRequest(eventoCom(MENSAGEM), contexto));
    assertTrue(contexto.logs.isEmpty());
  }
}
