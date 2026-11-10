package adjt.fase4.feedback.handler;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adjt.fase4.feedback.handler.dto.VersaoResponse;
import adjt.fase4.feedback.infrastructure.config.Ambiente;

/**
 * Função 4 — versão publicada ({@code GET /versao}).
 *
 * <p>Informa qual versão da aplicação está no ar e de qual commit ela saiu.
 * O pipeline consulta este endereço depois de cada deploy para confirmar que
 * a versão nova entrou antes de registrar a release. Os valores chegam por
 * variáveis de ambiente definidas no deploy; não há acesso a dados.
 */
public class VersaoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final String APLICACAO = "feedback-platform";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final VersaoResponse versao;

  /** Usado pelo Lambda: lê a versão e o commit definidos no deploy. */
  public VersaoHandler() {
    this(Ambiente.opcional("VERSAO", "desenvolvimento"), Ambiente.opcional("COMMIT", "local"));
  }

  public VersaoHandler(String versao, String commit) {
    this.versao = new VersaoResponse(APLICACAO, versao, commit);
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    String json;
    try {
      json = MAPPER.writeValueAsString(versao);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar a versão", e);
    }
    return new APIGatewayProxyResponseEvent()
        .withStatusCode(200)
        .withHeaders(Map.of("Content-Type", "application/json", "Cache-Control", "no-store"))
        .withBody(json);
  }
}
