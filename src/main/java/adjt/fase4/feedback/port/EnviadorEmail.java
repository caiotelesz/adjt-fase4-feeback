package adjt.fase4.feedback.port;

public interface EnviadorEmail {
  void enviar(String assunto, String corpo);
}