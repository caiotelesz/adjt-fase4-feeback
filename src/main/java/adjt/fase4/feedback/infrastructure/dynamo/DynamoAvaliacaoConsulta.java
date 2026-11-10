package adjt.fase4.feedback.infrastructure.dynamo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import adjt.fase4.feedback.application.port.AvaliacaoConsulta;
import adjt.fase4.feedback.domain.Avaliacao;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * Lê avaliações por período usando o índice secundário cuja chave de partição
 * é o dia do envio. Faz uma consulta por dia do período (oito, no relatório
 * semanal), de modo que o custo depende das avaliações do período, e não do
 * tamanho total da tabela, como aconteceria com uma varredura completa.
 */
public class DynamoAvaliacaoConsulta implements AvaliacaoConsulta {

  private static final String CONDICAO = AvaliacaoItem.DIA_ENVIO + " = :dia AND "
      + AvaliacaoItem.DATA_ENVIO + " BETWEEN :inicio AND :fim";

  private final DynamoDbClient dynamo;
  private final String tabela;
  private final String indicePorDia;

  public DynamoAvaliacaoConsulta(DynamoDbClient dynamo, String tabela, String indicePorDia) {
    this.dynamo = dynamo;
    this.tabela = tabela;
    this.indicePorDia = indicePorDia;
  }

  @Override
  public List<Avaliacao> buscarEntre(Instant inicio, Instant fim) {
    List<Avaliacao> resultado = new ArrayList<>();
    LocalDate ultimoDia = AvaliacaoItem.dia(fim);

    for (LocalDate dia = AvaliacaoItem.dia(inicio); !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
      QueryRequest consulta = QueryRequest.builder()
          .tableName(tabela)
          .indexName(indicePorDia)
          .keyConditionExpression(CONDICAO)
          .expressionAttributeValues(Map.of(
              ":dia", AvaliacaoItem.texto(dia.toString()),
              ":inicio", AvaliacaoItem.texto(AvaliacaoItem.dataEHora(inicio)),
              ":fim", AvaliacaoItem.texto(AvaliacaoItem.dataEHora(fim))))
          .build();

      for (QueryResponse pagina : dynamo.queryPaginator(consulta)) {
        for (Map<String, AttributeValue> item : pagina.items()) {
          resultado.add(AvaliacaoItem.paraAvaliacao(item));
        }
      }
    }
    return resultado;
  }
}
