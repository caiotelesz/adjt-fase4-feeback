package adjt.fase4.feedback.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;

/**
 * Escrita de log pelo logger do Lambda. Com o formato JSON configurado na
 * função, cada linha sai estruturada, com nível e identificador da requisição.
 */
final class Registro {

  private Registro() {
  }

  static void info(Context contexto, String mensagem) {
    registrar(contexto, mensagem, LogLevel.INFO);
  }

  static void erro(Context contexto, String mensagem) {
    registrar(contexto, mensagem, LogLevel.ERROR);
  }

  private static void registrar(Context contexto, String mensagem, LogLevel nivel) {
    if (contexto != null && contexto.getLogger() != null) {
      contexto.getLogger().log(mensagem, nivel);
    }
  }
}
