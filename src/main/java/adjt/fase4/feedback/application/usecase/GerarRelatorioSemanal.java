package adjt.fase4.feedback.application.usecase;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import adjt.fase4.feedback.application.port.AvaliacaoConsulta;
import adjt.fase4.feedback.application.port.EnviadorEmail;
import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.ResumoSemanal;

/**
 * Caso de uso: gerar o relatório dos últimos sete dias e enviá-lo por e-mail
 * aos administradores. O relatório é enviado mesmo sem avaliações no período,
 * para que a ausência de e-mail signifique falha, e não semana vazia.
 */
public class GerarRelatorioSemanal {

  static final String ASSUNTO = "Relatório semanal de feedbacks";
  static final Duration PERIODO = Duration.ofDays(7);

  private final AvaliacaoConsulta consulta;
  private final EnviadorEmail enviadorEmail;
  private final Clock relogio;
  private final ZoneId fuso;
  private final FormatadorRelatorioSemanal formatador;

  public GerarRelatorioSemanal(AvaliacaoConsulta consulta, EnviadorEmail enviadorEmail,
      Clock relogio, ZoneId fuso) {
    this.consulta = consulta;
    this.enviadorEmail = enviadorEmail;
    this.relogio = relogio;
    this.fuso = fuso;
    this.formatador = new FormatadorRelatorioSemanal(fuso);
  }

  /** Gera o relatório dos sete dias que terminam agora. */
  public ResumoSemanal executar() {
    return executar(relogio.instant());
  }

  /**
   * Gera o relatório dos sete dias que terminam em {@code fim}.
   *
   * <p>Na execução agendada, {@code fim} é o horário do agendamento, e não o
   * instante em que a função de fato rodou. Assim, os períodos de duas semanas
   * seguidas se encaixam sem deixar intervalo descoberto nem contar a mesma
   * avaliação duas vezes: cada período começa um milissegundo depois do fim
   * do anterior.
   *
   * @return o resumo calculado, para registro em log por quem chamou
   */
  public ResumoSemanal executar(Instant fim) {
    Instant inicio = fim.minus(PERIODO).plusMillis(1);

    List<Avaliacao> avaliacoes = consulta.buscarEntre(inicio, fim);
    ResumoSemanal resumo = ResumoSemanal.de(avaliacoes, fuso);

    enviadorEmail.enviar(ASSUNTO, formatador.formatar(resumo, inicio, fim));
    return resumo;
  }
}
