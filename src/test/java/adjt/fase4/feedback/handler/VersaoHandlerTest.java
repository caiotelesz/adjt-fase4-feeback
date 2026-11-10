package adjt.fase4.feedback.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;

class VersaoHandlerTest {

  @Test
  void deveInformarAVersaoEOCommitPublicados() {
    VersaoHandler handler = new VersaoHandler("1.0.3", "7874842");

    APIGatewayProxyResponseEvent resposta = handler.handleRequest(new APIGatewayProxyRequestEvent(), new ContextoFalso());

    assertEquals(200, resposta.getStatusCode());
    assertEquals("application/json", resposta.getHeaders().get("Content-Type"));
    assertEquals("no-store", resposta.getHeaders().get("Cache-Control"));
    assertEquals("{\"aplicacao\":\"feedback-platform\",\"versao\":\"1.0.3\",\"commit\":\"7874842\"}",
        resposta.getBody());
  }

  @Test
  void deveResponderMesmoSemContextoDoLambda() {
    VersaoHandler handler = new VersaoHandler("desenvolvimento", "local");

    assertEquals(200, handler.handleRequest(new APIGatewayProxyRequestEvent(), null).getStatusCode());
  }
}
