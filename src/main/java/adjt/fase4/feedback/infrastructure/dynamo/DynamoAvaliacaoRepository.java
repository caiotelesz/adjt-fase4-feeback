package adjt.fase4.feedback.infrastructure.dynamo;

import adjt.fase4.feedback.application.port.AvaliacaoRepository;
import adjt.fase4.feedback.domain.Avaliacao;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

/** Grava avaliações na tabela do DynamoDB. */
public class DynamoAvaliacaoRepository implements AvaliacaoRepository {

  private final DynamoDbClient dynamo;
  private final String tabela;

  public DynamoAvaliacaoRepository(DynamoDbClient dynamo, String tabela) {
    this.dynamo = dynamo;
    this.tabela = tabela;
  }

  @Override
  public void salvar(Avaliacao avaliacao) {
    dynamo.putItem(PutItemRequest.builder()
        .tableName(tabela)
        .item(AvaliacaoItem.paraItem(avaliacao))
        .build());
  }
}
