package adjt.fase4.feedback.infrastructure.dynamo;

import adjt.fase4.feedback.common.avaliacao.Avaliacao;
import adjt.fase4.feedback.common.model.Urgencia;
import adjt.fase4.feedback.port.AvaliacaoConsulta;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DynamoAvaliacaoConsulta implements AvaliacaoConsulta {

  private final DynamoDbClient dynamo;
  private final String tabela;

  public DynamoAvaliacaoConsulta(DynamoDbClient dynamo, String tabela) {
    this.dynamo = dynamo;
    this.tabela = tabela;
  }

  @Override
  public List<Avaliacao> buscarDesde(String dataInicio) {
    ScanRequest request = ScanRequest.builder()
        .tableName(tabela)
        .filterExpression("dataEnvio >= :inicio")
        .expressionAttributeValues(Map.of(
            ":inicio", AttributeValue.builder().s(dataInicio).build()))
        .build();

    List<Avaliacao> resultado = new ArrayList<>();

    for (ScanResponse pagina : dynamo.scanPaginator(request)) {
      for (Map<String, AttributeValue> item : pagina.items()) {
        resultado.add(paraAvaliacao(item));
      }
    }
    return resultado;
  }

  private Avaliacao paraAvaliacao(Map<String, AttributeValue> item) {
    return new Avaliacao(
        item.get("id").s(),
        item.get("descricao").s(),
        Integer.parseInt(item.get("nota").n()),
        Urgencia.valueOf(item.get("urgencia").s()),
        item.get("dataEnvio").s());
  }
}