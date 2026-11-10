package adjt.fase4.feedback.application.port;

import adjt.fase4.feedback.domain.Avaliacao;

/** Porta de saída: avisa, de forma assíncrona, que chegou um feedback crítico. */
public interface AlertaCritico {

  void publicar(Avaliacao avaliacao);
}
