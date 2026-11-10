package adjt.fase4.feedback.infrastructure.ses;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;

class SesEnviadorEmailTest {

  private final List<SendEmailRequest> envios = new ArrayList<>();

  private final SesClient ses = new SesClient() {
    @Override
    public SendEmailResponse sendEmail(SendEmailRequest request) {
      envios.add(request);
      return SendEmailResponse.builder().build();
    }

    @Override
    public String serviceName() {
      return "ses";
    }

    @Override
    public void close() {
    }
  };

  @Test
  void deveEnviarOEmailEmTextoDoRemetenteAoAdministrador() {
    new SesEnviadorEmail(ses, "remetente@exemplo.com", "admin@exemplo.com")
        .enviar("Relatório semanal de feedbacks", "Total de avaliações: 3");

    assertEquals(1, envios.size());
    SendEmailRequest envio = envios.get(0);
    assertEquals("remetente@exemplo.com", envio.source());
    assertEquals(List.of("admin@exemplo.com"), envio.destination().toAddresses());
    assertEquals("Relatório semanal de feedbacks", envio.message().subject().data());
    assertEquals("UTF-8", envio.message().subject().charset());
    assertEquals("Total de avaliações: 3", envio.message().body().text().data());
    assertEquals("UTF-8", envio.message().body().text().charset());
  }
}
