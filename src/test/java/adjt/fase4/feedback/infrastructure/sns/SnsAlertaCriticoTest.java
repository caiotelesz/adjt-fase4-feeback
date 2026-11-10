package adjt.fase4.feedback.infrastructure.sns;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.Nota;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

class SnsAlertaCriticoTest {

  private final List<PublishRequest> publicacoes = new ArrayList<>();

  private final SnsClient sns = new SnsClient() {
    @Override
    public PublishResponse publish(PublishRequest request) {
      publicacoes.add(request);
      return PublishResponse.builder().build();
    }

    @Override
    public String serviceName() {
      return "sns";
    }

    @Override
    public void close() {
    }
  };

  @Test
  void devePublicarAAvaliacaoNoTopicoComoJson() {
    Avaliacao avaliacao = new Avaliacao("abc-123", "Áudio ruim", new Nota(2),
        Instant.parse("2026-11-03T18:00:00Z"));

    new SnsAlertaCritico(sns, "arn:aws:sns:us-east-2:000000000000:alertas").publicar(avaliacao);

    assertEquals(1, publicacoes.size());
    assertEquals("arn:aws:sns:us-east-2:000000000000:alertas", publicacoes.get(0).topicArn());
    assertEquals("{\"id\":\"abc-123\",\"descricao\":\"Áudio ruim\",\"urgencia\":\"CRITICA\","
        + "\"dataEnvio\":\"2026-11-03T18:00:00Z\"}", publicacoes.get(0).message());
  }
}
