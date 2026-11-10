package adjt.fase4.feedback.domain;

/**
 * Grau de urgência de um feedback, derivado da nota.
 *
 * <ul>
 *   <li>0 a 3: {@link #CRITICA}, dispara o aviso imediato aos administradores;</li>
 *   <li>4 a 6: {@link #ATENCAO};</li>
 *   <li>7 a 10: {@link #OK}.</li>
 * </ul>
 */
public enum Urgencia {
  CRITICA,
  ATENCAO,
  OK;

  private static final int NOTA_MAXIMA_CRITICA = 3;
  private static final int NOTA_MAXIMA_ATENCAO = 6;

  public static Urgencia de(Nota nota) {
    if (nota.valor() <= NOTA_MAXIMA_CRITICA) {
      return CRITICA;
    }
    if (nota.valor() <= NOTA_MAXIMA_ATENCAO) {
      return ATENCAO;
    }
    return OK;
  }
}
