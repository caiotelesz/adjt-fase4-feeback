package adjt.fase4.feedback.infrastructure.sns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adjt.fase4.feedback.application.dto.NotificacaoCritica;
import adjt.fase4.feedback.application.port.AlertaCritico;
import adjt.fase4.feedback.domain.Avaliacao;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/** Publica o alerta de feedback crítico no tópico SNS, como JSON. */
public class SnsAlertaCritico implements AlertaCritico {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final SnsClient sns;
  private final String topicArn;

  public SnsAlertaCritico(SnsClient sns, String topicArn) {
    this.sns = sns;
    this.topicArn = topicArn;
  }

  @Override
  public void publicar(Avaliacao avaliacao) {
    String json;
    try {
      json = MAPPER.writeValueAsString(NotificacaoCritica.de(avaliacao));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar a notificação crítica", e);
    }

    sns.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(json)
        .build());
  }
}
