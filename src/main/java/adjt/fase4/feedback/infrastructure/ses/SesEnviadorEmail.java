package adjt.fase4.feedback.infrastructure.ses;

import adjt.fase4.feedback.port.EnviadorEmail;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;

public class SesEnviadorEmail implements EnviadorEmail {

  private final SesClient ses;
  private final String remetente;
  private final String destinatario;

  public SesEnviadorEmail(SesClient ses, String remetente, String destinatario) {
    this.ses = ses;
    this.remetente = remetente;
    this.destinatario = destinatario;
  }

  @Override
  public void enviar(String assunto, String corpo) {
    ses.sendEmail(SendEmailRequest.builder()
        .source(remetente)
        .destination(Destination.builder().toAddresses(destinatario).build())
        .message(Message.builder()
            .subject(Content.builder().data(assunto).charset("UTF-8").build())
            .body(Body.builder()
                .text(Content.builder().data(corpo).charset("UTF-8").build())
                .build())
            .build())
        .build());
  }
}