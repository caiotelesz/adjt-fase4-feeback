package adjt.fase4.feedback.application.port;

/** Porta de saída: envia um e-mail em texto aos administradores. */
public interface EnviadorEmail {

  void enviar(String assunto, String corpo);
}
