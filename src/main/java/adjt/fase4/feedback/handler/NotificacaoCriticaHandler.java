package adjt.fase4.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import adjt.fase4.feedback.application.dto.NotificacaoCritica;
import adjt.fase4.feedback.application.usecase.NotificarFeedbackCritico;
import adjt.fase4.feedback.infrastructure.config.Ambiente;
import adjt.fase4.feedback.infrastructure.config.ClientesAws;
import adjt.fase4.feedback.infrastructure.ses.SesEnviadorEmail;

/**
 * Função 2 — notificação de feedback crítico.
 *
 * <p>Acionada pelo tópico SNS de alertas críticos. Para cada mensagem, envia o
 * e-mail de aviso aos administradores. Qualquer falha é propagada de
 * propósito: o Lambda tenta de novo e, esgotadas as tentativas, a mensagem vai
 * para a fila de mensagens não entregues (DLQ), que tem alarme.
 */
public class NotificacaoCriticaHandler implements RequestHandler<SNSEvent, Void> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final NotificarFeedbackCritico notificarFeedbackCritico;

  /** Usado pelo Lambda: monta as dependências reais uma vez por ambiente de execução. */
  public NotificacaoCriticaHandler() {
    this(new NotificarFeedbackCritico(
        new SesEnviadorEmail(
            ClientesAws.ses(),
            Ambiente.obrigatoria("EMAIL_REMETENTE"),
            Ambiente.obrigatoria("EMAIL_ADMIN")),
        Ambiente.fusoHorario()));
  }

  public NotificacaoCriticaHandler(NotificarFeedbackCritico notificarFeedbackCritico) {
    this.notificarFeedbackCritico = notificarFeedbackCritico;
  }

  @Override
  public Void handleRequest(SNSEvent event, Context context) {
    for (SNSEvent.SNSRecord registro : event.getRecords()) {
      NotificacaoCritica notificacao;
      try {
        notificacao = MAPPER.readValue(registro.getSNS().getMessage(), NotificacaoCritica.class);
      } catch (JsonProcessingException e) {
        throw new IllegalStateException("Mensagem de alerta crítico inválida", e);
      }

      notificarFeedbackCritico.executar(notificacao);
      Registro.info(context, "Alerta crítico enviado por e-mail id=" + notificacao.id());
    }
    return null;
  }
}
