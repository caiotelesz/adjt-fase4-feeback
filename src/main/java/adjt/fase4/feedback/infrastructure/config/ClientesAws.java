package adjt.fase4.feedback.infrastructure.config;

import java.time.Duration;

import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sns.SnsClient;

/**
 * Cria os clientes da AWS com a mesma configuração: cliente HTTP leve (menor
 * cold start) e tempos-limite explícitos, para que uma dependência lenta não
 * consuma o tempo inteiro da função.
 */
public final class ClientesAws {

  private static final ClientOverrideConfiguration TEMPOS_LIMITE = ClientOverrideConfiguration.builder()
      .apiCallAttemptTimeout(Duration.ofSeconds(4))
      .apiCallTimeout(Duration.ofSeconds(10))
      .build();

  private ClientesAws() {
  }

  public static DynamoDbClient dynamo() {
    return DynamoDbClient.builder()
        .httpClient(UrlConnectionHttpClient.create())
        .overrideConfiguration(TEMPOS_LIMITE)
        .build();
  }

  public static SnsClient sns() {
    return SnsClient.builder()
        .httpClient(UrlConnectionHttpClient.create())
        .overrideConfiguration(TEMPOS_LIMITE)
        .build();
  }

  public static SesClient ses() {
    return SesClient.builder()
        .httpClient(UrlConnectionHttpClient.create())
        .overrideConfiguration(TEMPOS_LIMITE)
        .build();
  }
}
