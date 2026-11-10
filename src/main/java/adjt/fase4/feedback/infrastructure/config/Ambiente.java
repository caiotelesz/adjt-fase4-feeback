package adjt.fase4.feedback.infrastructure.config;

import java.time.ZoneId;

/** Leitura da configuração da função, toda feita por variáveis de ambiente. */
public final class Ambiente {

  private static final String FUSO_PADRAO = "America/Sao_Paulo";

  private Ambiente() {
  }

  /** Valor da variável, ou falha na inicialização com uma mensagem que diz qual falta. */
  public static String obrigatoria(String nome) {
    String valor = System.getenv(nome);
    if (valor == null || valor.isBlank()) {
      throw new IllegalStateException("Variável de ambiente obrigatória ausente: " + nome);
    }
    return valor;
  }

  /** Valor da variável, ou o padrão informado quando ela não está definida. */
  public static String opcional(String nome, String padrao) {
    String valor = System.getenv(nome);
    return valor == null || valor.isBlank() ? padrao : valor;
  }

  /** Fuso usado nos e-mails, definido em {@code FUSO_HORARIO} (padrão: Brasília). */
  public static ZoneId fusoHorario() {
    String valor = System.getenv("FUSO_HORARIO");
    return ZoneId.of(valor == null || valor.isBlank() ? FUSO_PADRAO : valor);
  }
}
