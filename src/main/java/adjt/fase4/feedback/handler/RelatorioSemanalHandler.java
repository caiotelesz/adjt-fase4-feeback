package adjt.fase4.feedback.handler;

import java.time.Clock;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import adjt.fase4.feedback.application.usecase.GerarRelatorioSemanal;
import adjt.fase4.feedback.domain.ResumoSemanal;
import adjt.fase4.feedback.infrastructure.config.Ambiente;
import adjt.fase4.feedback.infrastructure.config.ClientesAws;
import adjt.fase4.feedback.infrastructure.dynamo.DynamoAvaliacaoConsulta;
import adjt.fase4.feedback.infrastructure.ses.SesEnviadorEmail;

/**
 * Função 3 — relatório semanal.
 *
 * <p>Acionada pelo agendamento do EventBridge (toda segunda-feira). Consulta
 * as avaliações dos últimos sete dias e envia o relatório por e-mail aos
 * administradores. Falhas são propagadas para que a execução conte como erro
 * e dispare o alarme.
 */
public class RelatorioSemanalHandler implements RequestHandler<ScheduledEvent, Void> {

  private final GerarRelatorioSemanal gerarRelatorioSemanal;

  /** Usado pelo Lambda: monta as dependências reais uma vez por ambiente de execução. */
  public RelatorioSemanalHandler() {
    this(new GerarRelatorioSemanal(
        new DynamoAvaliacaoConsulta(
            ClientesAws.dynamo(),
            Ambiente.obrigatoria("TABELA_AVALIACOES"),
            Ambiente.obrigatoria("INDICE_POR_DIA")),
        new SesEnviadorEmail(
            ClientesAws.ses(),
            Ambiente.obrigatoria("EMAIL_REMETENTE"),
            Ambiente.obrigatoria("EMAIL_ADMIN")),
        Clock.systemUTC(),
        Ambiente.fusoHorario()));
  }

  public RelatorioSemanalHandler(GerarRelatorioSemanal gerarRelatorioSemanal) {
    this.gerarRelatorioSemanal = gerarRelatorioSemanal;
  }

  @Override
  public Void handleRequest(ScheduledEvent event, Context context) {
    ResumoSemanal resumo = gerarRelatorioSemanal.executar();
    Registro.info(context, "Relatório semanal enviado total=" + resumo.total());
    return null;
  }
}
