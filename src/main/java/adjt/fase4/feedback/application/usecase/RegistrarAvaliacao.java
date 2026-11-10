package adjt.fase4.feedback.application.usecase;

import java.time.Clock;

import adjt.fase4.feedback.application.port.AlertaCritico;
import adjt.fase4.feedback.application.port.AvaliacaoRepository;
import adjt.fase4.feedback.domain.Avaliacao;

/**
 * Caso de uso: receber o feedback de um estudante.
 *
 * <p>Valida, classifica a urgência, grava e, se for crítico, publica o alerta.
 * A gravação vem antes do alerta: um feedback aceito nunca é perdido. Se o
 * alerta não puder ser publicado, a avaliação continua registrada e a falha é
 * devolvida no {@link Resultado} para que a borda a torne visível (log e
 * alarme), em vez de rejeitar um feedback que já foi salvo.
 */
public class RegistrarAvaliacao {

  /**
   * @param falhaNoAlerta erro ao publicar o alerta crítico, ou {@code null}
   */
  public record Resultado(Avaliacao avaliacao, RuntimeException falhaNoAlerta) {

    public boolean alertaNaoPublicado() {
      return falhaNoAlerta != null;
    }
  }

  private final AvaliacaoRepository repository;
  private final AlertaCritico alerta;
  private final Clock relogio;

  public RegistrarAvaliacao(AvaliacaoRepository repository, AlertaCritico alerta, Clock relogio) {
    this.repository = repository;
    this.alerta = alerta;
    this.relogio = relogio;
  }

  /**
   * @throws adjt.fase4.feedback.domain.AvaliacaoInvalidaException se os dados violarem as regras
   */
  public Resultado executar(String descricao, Integer nota) {
    Avaliacao avaliacao = Avaliacao.nova(descricao, nota, relogio.instant());
    repository.salvar(avaliacao);

    if (!avaliacao.critica()) {
      return new Resultado(avaliacao, null);
    }
    try {
      alerta.publicar(avaliacao);
      return new Resultado(avaliacao, null);
    } catch (RuntimeException falha) {
      return new Resultado(avaliacao, falha);
    }
  }
}
