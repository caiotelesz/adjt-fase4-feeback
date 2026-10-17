package adjt.fase4.feedback.port;

import java.util.List;

import adjt.fase4.feedback.common.avaliacao.Avaliacao;

public interface AvaliacaoConsulta {
  List<Avaliacao> buscarDesde(String dataInicio);
}
