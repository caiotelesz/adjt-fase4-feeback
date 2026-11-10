package adjt.fase4.feedback.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Feedback de um estudante sobre uma aula.
 *
 * <p>As regras são verificadas no construtor, portanto valem tanto para uma
 * avaliação recém-recebida quanto para uma lida do banco: não existe
 * {@code Avaliacao} inválida em memória.
 */
public record Avaliacao(String id, String descricao, Nota nota, Instant dataEnvio) {

  public static final int TAMANHO_MAXIMO_DESCRICAO = 2000;

  public Avaliacao {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(dataEnvio, "dataEnvio");
    descricao = descricaoValida(descricao);
    if (nota == null) {
      throw new AvaliacaoInvalidaException("O campo 'nota' é obrigatório");
    }
    dataEnvio = dataEnvio.truncatedTo(ChronoUnit.MILLIS);
  }

  /** Registra uma nova avaliação, gerando seu identificador. */
  public static Avaliacao nova(String descricao, Integer nota, Instant dataEnvio) {
    String descricaoValida = descricaoValida(descricao);
    return new Avaliacao(UUID.randomUUID().toString(), descricaoValida, Nota.de(nota), dataEnvio);
  }

  public Urgencia urgencia() {
    return Urgencia.de(nota);
  }

  public boolean critica() {
    return urgencia() == Urgencia.CRITICA;
  }

  private static String descricaoValida(String descricao) {
    if (descricao == null || descricao.isBlank()) {
      throw new AvaliacaoInvalidaException("O campo 'descricao' é obrigatório");
    }
    String semEspacos = descricao.strip();
    if (semEspacos.length() > TAMANHO_MAXIMO_DESCRICAO) {
      throw new AvaliacaoInvalidaException(
          "O campo 'descricao' deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres");
    }
    return semEspacos;
  }
}
