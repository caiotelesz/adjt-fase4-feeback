package adjt.fase4.feedback.handler;

import adjt.fase4.feedback.common.avaliacao.Avaliacao;
import adjt.fase4.feedback.infrastructure.dynamo.DynamoAvaliacaoConsulta;
import adjt.fase4.feedback.infrastructure.ses.SesEnviadorEmail;
import adjt.fase4.feedback.port.AvaliacaoConsulta;
import adjt.fase4.feedback.port.EnviadorEmail;
import adjt.fase4.feedback.relatorio.RelatorioSemanal;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ses.SesClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RelatorioSemanalHandler implements RequestHandler<ScheduledEvent, Void> {

  private final AvaliacaoConsulta consulta;
  private final EnviadorEmail enviadorEmail;
  private final RelatorioSemanal relatorio = new RelatorioSemanal();

  public RelatorioSemanalHandler() {
    this(
        new DynamoAvaliacaoConsulta(
            DynamoDbClient.builder().httpClient(UrlConnectionHttpClient.create()).build(),
            System.getenv("TABELA_AVALIACOES")),
        new SesEnviadorEmail(
            SesClient.builder().httpClient(UrlConnectionHttpClient.create()).build(),
            System.getenv("EMAIL_REMETENTE"),
            System.getenv("EMAIL_ADMIN")));
  }

  public RelatorioSemanalHandler(AvaliacaoConsulta consulta, EnviadorEmail enviadorEmail) {
    this.consulta = consulta;
    this.enviadorEmail = enviadorEmail;
  }

  @Override
  public Void handleRequest(ScheduledEvent event, Context context) {
    String inicio = Instant.now().minus(7, ChronoUnit.DAYS).toString();
    List<Avaliacao> avaliacoes = consulta.buscarDesde(inicio);
    String texto = relatorio.gerar(avaliacoes);
    enviadorEmail.enviar("Relatório semanal de feedbacks", texto);
    return null;
  }
}