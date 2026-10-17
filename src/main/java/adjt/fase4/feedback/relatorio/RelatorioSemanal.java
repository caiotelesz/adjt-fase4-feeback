package adjt.fase4.feedback.relatorio;

import adjt.fase4.feedback.common.avaliacao.Avaliacao;
import adjt.fase4.feedback.common.model.Urgencia;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class RelatorioSemanal {

  public String gerar(List<Avaliacao> avaliacoes) {
    StringBuilder sb = new StringBuilder();
    sb.append("RELATÓRIO SEMANAL DE FEEDBACKS\n");
    sb.append("Total de avaliações: ").append(avaliacoes.size()).append("\n\n");

    if (avaliacoes.isEmpty()) {
      sb.append("Nenhuma avaliação recebida no período.\n");
      return sb.toString();
    }

    double media = avaliacoes.stream().mapToInt(Avaliacao::nota).average().orElse(0);
    sb.append(String.format(Locale.forLanguageTag("pt-BR"), "Média das notas: %.2f%n%n", media));

    sb.append("QUANTIDADE POR URGÊNCIA\n");
    Map<Urgencia, Long> porUrgencia = avaliacoes.stream()
        .collect(Collectors.groupingBy(Avaliacao::urgencia, Collectors.counting()));
    for (Urgencia urgencia : Urgencia.values()) {
      sb.append("- ").append(urgencia).append(": ")
          .append(porUrgencia.getOrDefault(urgencia, 0L)).append("\n");
    }

    sb.append("\nQUANTIDADE POR DIA\n");
    Map<String, Long> porDia = avaliacoes.stream()
        .collect(Collectors.groupingBy(a -> a.dataEnvio().substring(0, 10),
            TreeMap::new, Collectors.counting()));
    porDia.forEach((dia, qtd) -> sb.append("- ").append(dia).append(": ").append(qtd).append("\n"));

    sb.append("\nAVALIAÇÕES\n");
    avaliacoes.stream()
        .sorted(Comparator.comparing(Avaliacao::dataEnvio))
        .forEach(a -> sb.append("- [").append(a.urgencia()).append("] ")
            .append(a.dataEnvio()).append(" | nota ").append(a.nota())
            .append(" | ").append(a.descricao()).append("\n"));

    return sb.toString();
  }
}