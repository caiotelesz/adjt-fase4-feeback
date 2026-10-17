package adjt.fase4.feedback.infrastructure.dynamo;

import java.util.Map;

import adjt.fase4.feedback.common.avaliacao.Avaliacao;
import adjt.fase4.feedback.port.AvaliacaoRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class DynamoAvaliacaoRepository implements AvaliacaoRepository {

  private final DynamoDbClient dynamo;
  private final String tabela;

  public DynamoAvaliacaoRepository(DynamoDbClient dynamo, String tabela) {
    this.dynamo = dynamo;
    this.tabela = tabela;
  }

  @Override
  public void salvar(Avaliacao avaliacao) {
    Map<String, AttributeValue> item = Map.of(
        "id", AttributeValue.builder().s(avaliacao.id()).build(),
        "descricao", AttributeValue.builder().s(avaliacao.descricao()).build(),
        "nota", AttributeValue.builder().n(String.valueOf(avaliacao.nota())).build(),
        "urgencia", AttributeValue.builder().s(avaliacao.urgencia().name()).build(),
        "dataEnvio", AttributeValue.builder().s(avaliacao.dataEnvio()).build());

    dynamo.putItem(PutItemRequest.builder()
        .tableName(tabela)
        .item(item)
        .build());
  }
}
