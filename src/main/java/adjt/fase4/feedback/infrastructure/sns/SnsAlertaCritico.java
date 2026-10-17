package adjt.fase4.feedback.infrastructure.sns;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adjt.fase4.feedback.application.dto.email.NotificacaoCritica;
import adjt.fase4.feedback.common.avaliacao.Avaliacao;
import adjt.fase4.feedback.port.AlertaCritico;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;

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
    NotificacaoCritica notificacao = new NotificacaoCritica(
        avaliacao.id(),
        avaliacao.descricao(),
        avaliacao.urgencia().name(),
        avaliacao.dataEnvio());

    String json;
    try {
      json = MAPPER.writeValueAsString(notificacao);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar a notificação crítica", e);
    }

    sns.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .message(json)
        .build());
  }
}