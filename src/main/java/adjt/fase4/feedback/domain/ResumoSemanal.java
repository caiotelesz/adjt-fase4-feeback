package adjt.fase4.feedback.domain;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Números do relatório semanal: total, média das notas e quantidades por
 * urgência e por dia. Só calcula; a apresentação fica na camada de aplicação.
 */
public record ResumoSemanal(
    int total,
    double mediaDasNotas,
    Map<Urgencia, Long> quantidadePorUrgencia,
    SortedMap<LocalDate, Long> quantidadePorDia,
    List<Avaliacao> avaliacoes) {

  /**
   * @param fuso fuso horário usado para decidir a que dia cada avaliação pertence
   */
  public static ResumoSemanal de(List<Avaliacao> avaliacoes, ZoneId fuso) {
    Map<Urgencia, Long> porUrgencia = new EnumMap<>(Urgencia.class);
    for (Urgencia urgencia : Urgencia.values()) {
      porUrgencia.put(urgencia, 0L);
    }
    SortedMap<LocalDate, Long> porDia = new TreeMap<>();
    long somaDasNotas = 0;

    for (Avaliacao avaliacao : avaliacoes) {
      porUrgencia.merge(avaliacao.urgencia(), 1L, Long::sum);
      porDia.merge(LocalDate.ofInstant(avaliacao.dataEnvio(), fuso), 1L, Long::sum);
      somaDasNotas += avaliacao.nota().valor();
    }

    double media = avaliacoes.isEmpty() ? 0 : (double) somaDasNotas / avaliacoes.size();
    List<Avaliacao> emOrdemDeEnvio = avaliacoes.stream()
        .sorted(Comparator.comparing(Avaliacao::dataEnvio))
        .toList();

    return new ResumoSemanal(
        avaliacoes.size(),
        media,
        Collections.unmodifiableMap(porUrgencia),
        Collections.unmodifiableSortedMap(porDia),
        emOrdemDeEnvio);
  }

  public boolean vazio() {
    return total == 0;
  }
}
