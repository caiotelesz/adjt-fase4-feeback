package adjt.fase4.feedback.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import adjt.fase4.feedback.application.port.EnviadorEmail;
import adjt.fase4.feedback.application.usecase.GerarRelatorioSemanal;
import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.Nota;

class RelatorioSemanalHandlerTest {

  private static final Instant AGORA = Instant.parse("2026-11-09T12:00:00Z");

  private final List<String> corpos = new ArrayList<>();
  private final ContextoFalso contexto = new ContextoFalso();

  private static RelatorioSemanalHandler handlerCom(EnviadorEmail enviadorEmail) {
    return new RelatorioSemanalHandler(new GerarRelatorioSemanal(
        (inicio, fim) -> List.of(
            new Avaliacao("1", "Áudio ruim", new Nota(2), Instant.parse("2026-11-03T18:00:00Z"))),
        enviadorEmail,
        Clock.fixed(AGORA, ZoneOffset.UTC),
        ZoneId.of("America/Sao_Paulo")));
  }

  @Test
  void deveGerarEEnviarORelatorioQuandoOAgendamentoDisparar() {
    handlerCom((assunto, corpo) -> corpos.add(corpo)).handleRequest(new ScheduledEvent(), contexto);

    assertEquals(1, corpos.size());
    assertTrue(corpos.get(0).contains("Total de avaliações: 1"));
    assertEquals(List.of("Relatório semanal enviado total=1"), contexto.logs);
  }

  @Test
  void deveUsarOHorarioDoAgendamentoComoFimDoPeriodo() {
    List<Instant> periodos = new ArrayList<>();
    RelatorioSemanalHandler handler = new RelatorioSemanalHandler(new GerarRelatorioSemanal(
        (inicio, fim) -> {
          periodos.add(inicio);
          periodos.add(fim);
          return List.of();
        },
        (assunto, corpo) -> corpos.add(corpo),
        Clock.fixed(AGORA.plusSeconds(42), ZoneOffset.UTC),
        ZoneId.of("America/Sao_Paulo")));
    ScheduledEvent agendamento = new ScheduledEvent();
    agendamento.setTime(new org.joda.time.DateTime(AGORA.toEpochMilli()));

    handler.handleRequest(agendamento, contexto);

    assertEquals(List.of(Instant.parse("2026-11-02T12:00:00.001Z"), AGORA), periodos);
  }

  @Test
  void devePropagarAFalhaParaQueAExecucaoDispareOAlarme() {
    RelatorioSemanalHandler comFalha = handlerCom((assunto, corpo) -> {
      throw new IllegalStateException("SES indisponível");
    });

    assertThrows(IllegalStateException.class, () -> comFalha.handleRequest(new ScheduledEvent(), contexto));
  }
}
