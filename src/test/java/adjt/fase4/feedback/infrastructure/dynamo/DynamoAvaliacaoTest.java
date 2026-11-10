package adjt.fase4.feedback.infrastructure.dynamo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.Nota;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

class DynamoAvaliacaoTest {

  private final DynamoFalso dynamo = new DynamoFalso();
  private final DynamoAvaliacaoRepository repository = new DynamoAvaliacaoRepository(dynamo, "avaliacoes");
  private final DynamoAvaliacaoConsulta consulta = new DynamoAvaliacaoConsulta(dynamo, "avaliacoes", "por-dia");

  private Avaliacao gravar(String id, int nota, String dataEnvio) {
    Avaliacao avaliacao = new Avaliacao(id, "Feedback " + id, new Nota(nota), Instant.parse(dataEnvio));
    repository.salvar(avaliacao);
    return avaliacao;
  }

  @Test
  void deveGravarOItemComTodosOsAtributosEAChaveDoIndicePorDia() {
    gravar("abc-123", 2, "2026-11-03T18:00:00Z");

    assertEquals("avaliacoes", dynamo.gravacoes.get(0).tableName());
    Map<String, AttributeValue> item = dynamo.gravacoes.get(0).item();
    assertEquals("abc-123", item.get("id").s());
    assertEquals("Feedback abc-123", item.get("descricao").s());
    assertEquals("2", item.get("nota").n());
    assertEquals("CRITICA", item.get("urgencia").s());
    assertEquals("2026-11-03T18:00:00.000Z", item.get("dataEnvio").s());
    assertEquals("2026-11-03", item.get("diaEnvio").s());
  }

  @Test
  void deveLerDeVoltaExatamenteAAvaliacaoGravada() {
    Avaliacao gravada = gravar("abc-123", 7, "2026-11-03T18:00:00.123Z");

    List<Avaliacao> lidas = consulta.buscarEntre(
        Instant.parse("2026-11-03T00:00:00Z"), Instant.parse("2026-11-03T23:59:59Z"));

    assertEquals(List.of(gravada), lidas);
  }

  @Test
  void deveTrazerSomenteAsAvaliacoesDoPeriodoIncluindoOsLimites() {
    gravar("antes", 5, "2026-11-02T11:59:59.999Z");
    Avaliacao noInicio = gravar("inicio", 5, "2026-11-02T12:00:00Z");
    Avaliacao noMeio = gravar("meio", 5, "2026-11-05T08:00:00Z");
    Avaliacao noFim = gravar("fim", 5, "2026-11-09T12:00:00Z");
    gravar("depois", 5, "2026-11-09T12:00:00.001Z");

    List<Avaliacao> lidas = consulta.buscarEntre(
        Instant.parse("2026-11-02T12:00:00Z"), Instant.parse("2026-11-09T12:00:00Z"));

    assertEquals(List.of(noInicio, noMeio, noFim), lidas);
  }

  @Test
  void deveConsultarOIndicePorDiaUmaVezParaCadaDiaDoPeriodo() {
    consulta.buscarEntre(Instant.parse("2026-11-02T12:00:00Z"), Instant.parse("2026-11-09T12:00:00Z"));

    assertEquals(8, dynamo.consultas.size());
    QueryRequest primeira = dynamo.consultas.get(0);
    assertEquals("avaliacoes", primeira.tableName());
    assertEquals("por-dia", primeira.indexName());
    assertEquals("diaEnvio = :dia AND dataEnvio BETWEEN :inicio AND :fim", primeira.keyConditionExpression());
    assertEquals("2026-11-02", primeira.expressionAttributeValues().get(":dia").s());
    assertEquals("2026-11-09", dynamo.consultas.get(7).expressionAttributeValues().get(":dia").s());
  }

  @Test
  void deveLerTodasAsPaginasDeUmMesmoDia() {
    gravar("1", 5, "2026-11-03T10:00:00Z");
    gravar("2", 5, "2026-11-03T11:00:00Z");
    gravar("3", 5, "2026-11-03T12:00:00Z");

    List<Avaliacao> lidas = consulta.buscarEntre(
        Instant.parse("2026-11-03T00:00:00Z"), Instant.parse("2026-11-03T23:59:59Z"));

    assertEquals(3, lidas.size());
    assertTrue(dynamo.consultas.size() >= 3);
  }

  @Test
  void deveOrdenarCronologicamenteDatasComESemFracaoDeSegundo() {
    Avaliacao semFracao = gravar("sem-fracao", 5, "2026-11-03T10:00:00Z");
    gravar("com-fracao", 5, "2026-11-03T10:00:00.500Z");

    List<Avaliacao> lidas = consulta.buscarEntre(
        Instant.parse("2026-11-03T09:00:00Z"), Instant.parse("2026-11-03T10:00:00.100Z"));

    assertEquals(List.of(semFracao), lidas);
  }
}
