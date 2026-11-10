package adjt.fase4.feedback.handler.dto;

/** Corpo do {@code POST /avaliacao}. */
public record AvaliacaoRequest(String descricao, Integer nota) {
}
