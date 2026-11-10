package adjt.fase4.feedback.infrastructure.dynamo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * Cliente do DynamoDB em memória: guarda os itens gravados e responde às
 * consultas aplicando a condição de chave do índice por dia, uma página por
 * item, para exercitar também a paginação.
 */
class DynamoFalso implements DynamoDbClient {

  final List<PutItemRequest> gravacoes = new ArrayList<>();
  final List<QueryRequest> consultas = new ArrayList<>();

  @Override
  public PutItemResponse putItem(PutItemRequest request) {
    gravacoes.add(request);
    return PutItemResponse.builder().build();
  }

  @Override
  public QueryResponse query(QueryRequest request) {
    consultas.add(request);
    Map<String, AttributeValue> valores = request.expressionAttributeValues();
    String dia = valores.get(":dia").s();
    String inicio = valores.get(":inicio").s();
    String fim = valores.get(":fim").s();

    List<Map<String, AttributeValue>> encontrados = gravacoes.stream()
        .map(PutItemRequest::item)
        .filter(item -> item.get("diaEnvio").s().equals(dia))
        .filter(item -> item.get("dataEnvio").s().compareTo(inicio) >= 0)
        .filter(item -> item.get("dataEnvio").s().compareTo(fim) <= 0)
        .toList();

    int posicao = request.hasExclusiveStartKey()
        ? Integer.parseInt(request.exclusiveStartKey().get("posicao").n())
        : 0;
    if (posicao >= encontrados.size()) {
      return QueryResponse.builder().items(List.of()).build();
    }
    QueryResponse.Builder pagina = QueryResponse.builder().items(List.of(encontrados.get(posicao)));
    if (posicao + 1 < encontrados.size()) {
      pagina.lastEvaluatedKey(Map.of("posicao",
          AttributeValue.builder().n(String.valueOf(posicao + 1)).build()));
    }
    return pagina.build();
  }

  @Override
  public String serviceName() {
    return "dynamodb";
  }

  @Override
  public void close() {
  }
}
