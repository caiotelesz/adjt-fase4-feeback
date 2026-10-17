package adjt.fase4.feedback.handler;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import adjt.fase4.feedback.application.dto.request.AvaliacaoRequest;
import adjt.fase4.feedback.application.dto.response.AvaliacaoResponse;
import adjt.fase4.feedback.common.avaliacao.Avaliacao;
import adjt.fase4.feedback.common.model.Urgencia;
import adjt.fase4.feedback.infrastructure.dynamo.DynamoAvaliacaoRepository;
import adjt.fase4.feedback.infrastructure.sns.SnsAlertaCritico;
import adjt.fase4.feedback.port.AlertaCritico;
import adjt.fase4.feedback.port.AvaliacaoRepository;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.sns.SnsClient;

public class AvaliacaoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private static final ObjectMapper MAPPER = new ObjectMapper()
      .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);

  private final AvaliacaoRepository repository;
  private final AlertaCritico alerta;

  public AvaliacaoHandler(AvaliacaoRepository repository, AlertaCritico alerta) {
    this.repository = repository;
    this.alerta = alerta;
  }

  public AvaliacaoHandler() {
    this(
        new DynamoAvaliacaoRepository(
            DynamoDbClient.builder().httpClient(UrlConnectionHttpClient.create()).build(),
            System.getenv("TABELA_AVALIACOES")),
        new SnsAlertaCritico(
            SnsClient.builder().httpClient(UrlConnectionHttpClient.create()).build(),
            System.getenv("TOPICO_CRITICO_ARN")));
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
    try {

      String body = event.getBody();
      if (body == null || body.isBlank()) {
        return erro(400, "O corpo da requisição é obrigatório");
      }

      AvaliacaoRequest request;
      try {
        request = MAPPER.readValue(body, AvaliacaoRequest.class);
      } catch (JsonProcessingException e) {
        return erro(400, "JSON inválido");
      }

      String erroValidacao = validar(request);
      if (erroValidacao != null) {
        return erro(400, erroValidacao);
      }

      Avaliacao avaliacao = Avaliacao.nova(request.descricao(), request.nota());
      repository.salvar(avaliacao);

      if (avaliacao.urgencia() == Urgencia.CRITICA) {
        alerta.publicar(avaliacao);
      }

      AvaliacaoResponse response = new AvaliacaoResponse(
          avaliacao.id(),
          avaliacao.descricao(),
          avaliacao.nota(),
          avaliacao.urgencia(),
          avaliacao.dataEnvio());
      return resposta(201, response);

    } catch (RuntimeException e) {

      if (context != null) {
        context.getLogger().log("Erro ao processar avaliação: " + e);
      }
      return erro(500, "Erro interno ao registrar a avaliação");
    }
  }

  private String validar(AvaliacaoRequest request) {
    if (request == null) {
      return "O corpo da requisição é obrigatório";
    }
    if (request.descricao() == null || request.descricao().isBlank()) {
      return "O campo 'descricao' é obrigatório";
    }
    if (request.nota() == null) {
      return "O campo 'nota' é obrigatório";
    }
    if (request.nota() < 0 || request.nota() > 10) {
      return "A nota deve estar entre 0 e 10";
    }
    return null;
  }

  private APIGatewayProxyResponseEvent erro(int status, String mensagem) {
    return resposta(status, Map.of("erro", mensagem));
  }

  private APIGatewayProxyResponseEvent resposta(int status, Object corpo) {
    String json;
    try {
      json = MAPPER.writeValueAsString(corpo);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Falha ao serializar a resposta", e);
    }
    return new APIGatewayProxyResponseEvent()
        .withStatusCode(status)
        .withHeaders(Map.of("Content-Type", "application/json"))
        .withBody(json);
  }
}
