package adjt.fase4.feedback.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

import adjt.fase4.feedback.application.port.AlertaCritico;
import adjt.fase4.feedback.application.port.AvaliacaoRepository;
import adjt.fase4.feedback.application.usecase.RegistrarAvaliacao;
import adjt.fase4.feedback.domain.Avaliacao;

class AvaliacaoHandlerTest {

  private static final Clock RELOGIO = Clock.fixed(Instant.parse("2026-11-03T18:00:00Z"), ZoneOffset.UTC);

  private final List<Avaliacao> salvas = new ArrayList<>();
  private final List<Avaliacao> alertas = new ArrayList<>();
  private final ContextoFalso contexto = new ContextoFalso();
  private AvaliacaoHandler handler = handlerCom(salvas::add, alertas::add);

  private static AvaliacaoHandler handlerCom(AvaliacaoRepository repository, AlertaCritico alerta) {
    return new AvaliacaoHandler(new RegistrarAvaliacao(repository, alerta, RELOGIO));
  }

  private APIGatewayProxyResponseEvent enviar(String corpo) {
    return handler.handleRequest(new APIGatewayProxyRequestEvent().withBody(corpo), contexto);
  }

  @Test
  void deveRegistrarAAvaliacaoEResponder201ComOsDadosGravados() {
    APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"Aula ótima\",\"nota\":9}");

    assertEquals(201, resposta.getStatusCode());
    assertEquals("application/json", resposta.getHeaders().get("Content-Type"));
    assertEquals(1, salvas.size());
    assertEquals("{\"id\":\"" + salvas.get(0).id() + "\",\"descricao\":\"Aula ótima\",\"nota\":9,"
        + "\"urgencia\":\"OK\",\"dataEnvio\":\"2026-11-03T18:00:00Z\"}", resposta.getBody());
    assertTrue(alertas.isEmpty());
  }

  @Test
  void devePublicarAlertaQuandoAAvaliacaoForCritica() {
    APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"Áudio ruim\",\"nota\":3}");

    assertEquals(201, resposta.getStatusCode());
    assertTrue(resposta.getBody().contains("\"urgencia\":\"CRITICA\""));
    assertEquals(salvas, alertas);
  }

  @ParameterizedTest
  @CsvSource(delimiter = '|', value = {
      "{\"nota\":5}                                 | O campo 'descricao' é obrigatório",
      "{\"descricao\":\"   \",\"nota\":5}           | O campo 'descricao' é obrigatório",
      "{\"descricao\":\"Aula\"}                     | O campo 'nota' é obrigatório",
      "{\"descricao\":\"Aula\",\"nota\":null}       | O campo 'nota' é obrigatório",
      "{\"descricao\":\"Aula\",\"nota\":-1}         | A nota deve estar entre 0 e 10",
      "{\"descricao\":\"Aula\",\"nota\":11}         | A nota deve estar entre 0 e 10",
      "{\"descricao\":\"Aula\",\"nota\":7.5}        | O campo 'nota' deve ser um número inteiro",
      "{\"descricao\":\"Aula\",\"nota\":\"7\"}      | O campo 'nota' deve ser um número inteiro",
      "{\"descricao\":\"Aula\",\"nota\":true}       | O campo 'nota' deve ser um número inteiro",
      "{\"descricao\":123,\"nota\":7}               | O campo 'descricao' deve ser um texto",
      "{\"descricao\":\"Aula\",\"nota\":7,\"x\":1}  | Campo desconhecido: 'x'",
      "[]                                           | O corpo da requisição deve ser um objeto JSON",
      "{descricao                                   | JSON inválido",
      "null                                         | O corpo da requisição é obrigatório"
  })
  void deveResponder400SemGravarQuandoARequisicaoForInvalida(String corpo, String mensagem) {
    APIGatewayProxyResponseEvent resposta = enviar(corpo);

    assertEquals(400, resposta.getStatusCode());
    assertEquals("{\"erro\":\"" + mensagem + "\"}", resposta.getBody());
    assertTrue(salvas.isEmpty());
    assertTrue(alertas.isEmpty());
  }

  @ParameterizedTest
  @ValueSource(strings = { "", "   " })
  void deveResponder400QuandoOCorpoEstiverVazio(String corpo) {
    APIGatewayProxyResponseEvent resposta = enviar(corpo);

    assertEquals(400, resposta.getStatusCode());
    assertEquals("{\"erro\":\"O corpo da requisição é obrigatório\"}", resposta.getBody());
  }

  @Test
  void deveResponder400QuandoNaoHouverCorpo() {
    assertEquals(400, enviar(null).getStatusCode());
  }

  @Test
  void deveResponder500SemExporDetalhesERegistrarOErroQuandoAGravacaoFalhar() {
    handler = handlerCom(avaliacao -> {
      throw new IllegalStateException("DynamoDB indisponível");
    }, alertas::add);

    APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"Áudio ruim\",\"nota\":1}");

    assertEquals(500, resposta.getStatusCode());
    assertEquals("{\"erro\":\"Erro interno ao registrar a avaliação\"}", resposta.getBody());
    assertTrue(alertas.isEmpty());
    assertTrue(contexto.logs.get(0).contains("DynamoDB indisponível"));
  }

  @Test
  void deveResponder201ERegistrarOMarcadorDoAlarmeQuandoOAlertaNaoForPublicado() {
    handler = handlerCom(salvas::add, avaliacao -> {
      throw new IllegalStateException("SNS indisponível");
    });

    APIGatewayProxyResponseEvent resposta = enviar("{\"descricao\":\"Áudio ruim\",\"nota\":1}");

    assertEquals(201, resposta.getStatusCode());
    assertEquals(1, salvas.size());
    assertEquals(1, contexto.logs.size());
    assertTrue(contexto.logs.get(0).startsWith("ALERTA_CRITICO_NAO_PUBLICADO id=" + salvas.get(0).id()));
  }

  @Test
  void naoDeveRegistrarOMarcadorDoAlarmeQuandoOAlertaForPublicado() {
    enviar("{\"descricao\":\"Áudio ruim\",\"nota\":1}");

    assertFalse(contexto.logs.stream().anyMatch(log -> log.contains("ALERTA_CRITICO_NAO_PUBLICADO")));
  }

  @Test
  void deveFuncionarSemContextoDoLambda() {
    APIGatewayProxyResponseEvent resposta = handler.handleRequest(
        new APIGatewayProxyRequestEvent().withBody("{\"descricao\":\"Aula\",\"nota\":8}"), null);

    assertEquals(201, resposta.getStatusCode());
  }
}
