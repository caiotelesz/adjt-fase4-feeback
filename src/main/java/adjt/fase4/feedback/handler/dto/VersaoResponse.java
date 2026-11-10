package adjt.fase4.feedback.handler.dto;

/**
 * Resposta do {@code GET /versao}.
 *
 * @param versao versão semântica publicada (por exemplo, 1.0.3)
 * @param commit identificador curto do commit que gerou a publicação
 */
public record VersaoResponse(String aplicacao, String versao, String commit) {
}
