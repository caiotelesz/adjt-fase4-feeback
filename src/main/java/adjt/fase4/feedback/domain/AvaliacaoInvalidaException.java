package adjt.fase4.feedback.domain;

/**
 * Lançada quando os dados informados violam uma regra de negócio da avaliação.
 * A mensagem é própria para ser devolvida a quem enviou o feedback.
 */
public class AvaliacaoInvalidaException extends RuntimeException {

  public AvaliacaoInvalidaException(String mensagem) {
    super(mensagem);
  }
}
