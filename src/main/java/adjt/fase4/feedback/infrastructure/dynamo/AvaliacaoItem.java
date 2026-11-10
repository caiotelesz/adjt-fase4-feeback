package adjt.fase4.feedback.infrastructure.dynamo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.Nota;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Conversão entre {@link Avaliacao} e o item gravado no DynamoDB.
 *
 * <p>Além dos dados da avaliação, o item leva {@code diaEnvio} (chave de
 * partição do índice de consulta por período) e {@code urgencia} (redundante
 * com a nota, mantida para leitura direta no console e filtros futuros).
 * As datas são gravadas em UTC com tamanho fixo, para que a ordem
 * alfabética da chave de ordenação seja também a ordem cronológica.
 */
final class AvaliacaoItem {

  static final String ID = "id";
  static final String DESCRICAO = "descricao";
  static final String NOTA = "nota";
  static final String URGENCIA = "urgencia";
  static final String DATA_ENVIO = "dataEnvio";
  static final String DIA_ENVIO = "diaEnvio";

  private static final DateTimeFormatter DATA_E_HORA = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      .withZone(ZoneOffset.UTC);

  private AvaliacaoItem() {
  }

  static Map<String, AttributeValue> paraItem(Avaliacao avaliacao) {
    return Map.of(
        ID, texto(avaliacao.id()),
        DESCRICAO, texto(avaliacao.descricao()),
        NOTA, AttributeValue.builder().n(String.valueOf(avaliacao.nota().valor())).build(),
        URGENCIA, texto(avaliacao.urgencia().name()),
        DATA_ENVIO, texto(dataEHora(avaliacao.dataEnvio())),
        DIA_ENVIO, texto(dia(avaliacao.dataEnvio()).toString()));
  }

  static Avaliacao paraAvaliacao(Map<String, AttributeValue> item) {
    return new Avaliacao(
        item.get(ID).s(),
        item.get(DESCRICAO).s(),
        new Nota(Integer.parseInt(item.get(NOTA).n())),
        Instant.parse(item.get(DATA_ENVIO).s()));
  }

  static String dataEHora(Instant instante) {
    return DATA_E_HORA.format(instante);
  }

  static LocalDate dia(Instant instante) {
    return LocalDate.ofInstant(instante, ZoneOffset.UTC);
  }

  static AttributeValue texto(String valor) {
    return AttributeValue.builder().s(valor).build();
  }
}
