package adjt.fase4.feedback.handler;

import java.time.Clock;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;

import adjt.fase4.feedback.application.usecase.RegistrarAvaliacao;
import adjt.fase4.feedback.domain.AvaliacaoInvalidaException;
import adjt.fase4.feedback.handler.dto.AvaliacaoRequest;
import adjt.fase4.feedback.handler.dto.AvaliacaoResponse;
import adjt.fase4.feedback.infrastructure.config.Ambiente;
import adjt.fase4.feedback.infrastructure.config.ClientesAws;
import adjt.fase4.feedback.infrastructure.dynamo.DynamoAvaliacaoRepository;
import adjt.fase4.feedback.infrastructure.sns.SnsAlertaCritico;

/**
 * Função 1 — recebimento de feedbacks ({@code POST /avaliacao}).
 *
 * <p>Adaptador HTTP: traduz a requisição do API Gateway para o caso de uso
 * {@link RegistrarAvaliacao} e o resultado para uma resposta HTTP.
 *
 * <ul>
 *   <li>201: avaliação registrada;</li>
 *   <li>400: corpo ausente, JSON malformado ou regra de negócio violada;</li>
 *   <li>500: falha inesperada (o detalhe vai para o log, não para o cliente).</li>
 * </ul>
 */
public class AvaliacaoHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  /** Texto procurado pelo filtro de métrica que alimenta o alarme de alerta não publicado. */
  static final String ALERTA_NAO_PUBLICADO = "ALERTA_CRITICO_NAO_PUBLICADO";

  private static final ObjectMapper MAPPER = criarMapper();

  private final RegistrarAvaliacao registrarAvaliacao;

  /** Usado pelo Lambda: monta as dependências reais uma vez por ambiente de execução. */
  public AvaliacaoHandler() {
    this(new RegistrarAvaliacao(
        new DynamoAvaliacaoRepository(ClientesAws.dynamo(), Ambiente.obrigatoria("TABELA_AVALIACOES")),
        new SnsAlertaCritico(ClientesAws.sns(), Ambiente.obrigatoria("TOPICO_CRITICO_ARN")),
        Clock.systemUTC()));
  }

  public AvaliacaoHandler(RegistrarAvaliacao registrarAvaliacao) {
    this.registrarAvaliacao = registrarAvaliacao;
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
      } catch (UnrecognizedPropertyException e) {
        return erro(400, "Campo desconhecido: '" + e.getPropertyName() + "'");
      } catch (MismatchedInputException e) {
        return erro(400, mensagemDeTipoInvalido(e));
      } catch (JsonProcessingException e) {
        return erro(400, "JSON inválido");
      }
      if (request == null) {
        return erro(400, "O corpo da requisição é obrigatório");
      }

      RegistrarAvaliacao.Resultado resultado;
      try {
        resultado = registrarAvaliacao.executar(request.descricao(), request.nota());
      } catch (AvaliacaoInvalidaException e) {
        return erro(400, e.getMessage());
      }

      if (resultado.alertaNaoPublicado()) {
        Registro.erro(context, ALERTA_NAO_PUBLICADO + " id=" + resultado.avaliacao().id()
            + " causa=" + resultado.falhaNoAlerta());
      }
      return resposta(201, AvaliacaoResponse.de(resultado.avaliacao()));

    } catch (RuntimeException e) {
      Registro.erro(context, "Erro ao processar avaliação: " + e);
      return erro(500, "Erro interno ao registrar a avaliação");
    }
  }

  private static ObjectMapper criarMapper() {
    ObjectMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
        .disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
        .build();
    mapper.coercionConfigFor(LogicalType.Textual)
        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
    return mapper;
  }

  private static String mensagemDeTipoInvalido(MismatchedInputException e) {
    if (e.getPath().isEmpty() || e.getPath().get(0).getFieldName() == null) {
      return "O corpo da requisição deve ser um objeto JSON";
    }
    String campo = e.getPath().get(0).getFieldName();
    return "nota".equals(campo)
        ? "O campo 'nota' deve ser um número inteiro"
        : "O campo '" + campo + "' deve ser um texto";
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
