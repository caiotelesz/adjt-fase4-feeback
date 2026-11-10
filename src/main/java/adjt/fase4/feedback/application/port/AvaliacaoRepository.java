package adjt.fase4.feedback.application.port;

import adjt.fase4.feedback.domain.Avaliacao;

/** Porta de saída: grava uma avaliação recebida. */
public interface AvaliacaoRepository {

  void salvar(Avaliacao avaliacao);
}
