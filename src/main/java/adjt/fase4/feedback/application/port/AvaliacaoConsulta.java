package adjt.fase4.feedback.application.port;

import java.time.Instant;
import java.util.List;

import adjt.fase4.feedback.domain.Avaliacao;

/** Porta de saída: lê as avaliações enviadas em um período. */
public interface AvaliacaoConsulta {

  /** Avaliações com data de envio entre {@code inicio} e {@code fim}, inclusive. */
  List<Avaliacao> buscarEntre(Instant inicio, Instant fim);
}
