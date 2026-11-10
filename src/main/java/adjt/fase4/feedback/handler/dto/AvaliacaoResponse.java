package adjt.fase4.feedback.handler.dto;

import adjt.fase4.feedback.domain.Avaliacao;

/**
 * Resposta do {@code POST /avaliacao} quando a avaliação é registrada.
 *
 * @param dataEnvio instante do registro em ISO-8601 (UTC)
 */
public record AvaliacaoResponse(String id, String descricao, int nota, String urgencia, String dataEnvio) {

  public static AvaliacaoResponse de(Avaliacao avaliacao) {
    return new AvaliacaoResponse(
        avaliacao.id(),
        avaliacao.descricao(),
        avaliacao.nota().valor(),
        avaliacao.urgencia().name(),
        avaliacao.dataEnvio().toString());
  }
}
