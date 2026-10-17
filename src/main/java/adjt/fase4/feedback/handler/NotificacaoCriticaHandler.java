package adjt.fase4.feedback.handler;

import adjt.fase4.feedback.application.dto.email.NotificacaoCritica;
import adjt.fase4.feedback.infrastructure.ses.SesEnviadorEmail;
import adjt.fase4.feedback.port.EnviadorEmail;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.ses.SesClient;

public class NotificacaoCriticaHandler implements RequestHandler<SNSEvent, Void> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final EnviadorEmail enviadorEmail;

  public NotificacaoCriticaHandler() {
    this(new SesEnviadorEmail(
        SesClient.builder().httpClient(UrlConnectionHttpClient.create()).build(),
        System.getenv("EMAIL_REMETENTE"),
        System.getenv("EMAIL_ADMIN")));
  }

  public NotificacaoCriticaHandler(EnviadorEmail enviadorEmail) {
    this.enviadorEmail = enviadorEmail;
  }

  @Override
  public Void handleRequest(SNSEvent event, Context context) {
    for (SNSEvent.SNSRecord record : event.getRecords()) {
      String mensagem = record.getSNS().getMessage();

      NotificacaoCritica notificacao;
      try {
        notificacao = MAPPER.readValue(mensagem, NotificacaoCritica.class);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException("Mensagem do SNS inválida: " + mensagem, e);
      }

      String assunto = "[FEEDBACK CRÍTICO] Novo feedback de aula";
      String corpo = """
          Um feedback crítico foi recebido.

          Descrição: %s
          Urgência: %s
          Data de envio: %s
          """.formatted(notificacao.descricao(), notificacao.urgencia(), notificacao.dataEnvio());

      enviadorEmail.enviar(assunto, corpo);
    }
    return null;
  }
}