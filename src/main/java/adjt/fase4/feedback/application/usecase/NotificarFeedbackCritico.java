package adjt.fase4.feedback.application.usecase;

import java.time.Instant;
import java.time.ZoneId;

import adjt.fase4.feedback.application.dto.NotificacaoCritica;
import adjt.fase4.feedback.application.port.EnviadorEmail;

/**
 * Caso de uso: avisar os administradores, por e-mail, de um feedback crítico.
 * O e-mail leva a descrição, a urgência e a data de envio.
 */
public class NotificarFeedbackCritico {

  static final String ASSUNTO = "[FEEDBACK CRÍTICO] Novo feedback de aula";

  private final EnviadorEmail enviadorEmail;
  private final FormatadorDeData datas;

  public NotificarFeedbackCritico(EnviadorEmail enviadorEmail, ZoneId fuso) {
    this.enviadorEmail = enviadorEmail;
    this.datas = new FormatadorDeData(fuso);
  }

  public void executar(NotificacaoCritica notificacao) {
    String corpo = """
        Um feedback crítico foi recebido.

        Descrição: %s
        Urgência: %s
        Data de envio: %s (%s)
        Identificador: %s
        """.formatted(
        notificacao.descricao(),
        notificacao.urgencia(),
        datas.diaEHora(Instant.parse(notificacao.dataEnvio())),
        datas.fuso(),
        notificacao.id());

    enviadorEmail.enviar(ASSUNTO, corpo);
  }
}
