package adjt.fase4.feedback.domain;

/**
 * Nota atribuída a uma aula, de {@value #MINIMA} a {@value #MAXIMA}.
 * É o único lugar do sistema que conhece o intervalo válido.
 */
public record Nota(int valor) {

  public static final int MINIMA = 0;
  public static final int MAXIMA = 10;

  public Nota {
    if (valor < MINIMA || valor > MAXIMA) {
      throw new AvaliacaoInvalidaException(
          "A nota deve estar entre " + MINIMA + " e " + MAXIMA);
    }
  }

  /** Cria a nota a partir de um valor que pode não ter sido informado. */
  public static Nota de(Integer valor) {
    if (valor == null) {
      throw new AvaliacaoInvalidaException("O campo 'nota' é obrigatório");
    }
    return new Nota(valor);
  }
}
