package adjt.fase4.feedback.application.dto;

import adjt.fase4.feedback.domain.Avaliacao;

/**
 * Mensagem que trafega no tópico de alertas críticos. É o contrato entre a
 * função que recebe a avaliação e a função que envia o e-mail.
 *
 * @param dataEnvio instante do envio em ISO-8601 (UTC)
 */
public record NotificacaoCritica(String id, String descricao, String urgencia, String dataEnvio) {

  public static NotificacaoCritica de(Avaliacao avaliacao) {
    return new NotificacaoCritica(
        avaliacao.id(),
        avaliacao.descricao(),
        avaliacao.urgencia().name(),
        avaliacao.dataEnvio().toString());
  }
}
