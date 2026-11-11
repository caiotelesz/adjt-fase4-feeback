package adjt.fase4.feedback.application.usecase;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import adjt.fase4.feedback.domain.Avaliacao;
import adjt.fase4.feedback.domain.ResumoSemanal;

/** Transforma o {@link ResumoSemanal} no texto do e-mail enviado aos administradores. */
final class FormatadorRelatorioSemanal {

  /** Limites da listagem, para o e-mail não crescer sem controle em semanas de muito volume. */
  static final int MAXIMO_DE_AVALIACOES_LISTADAS = 200;
  static final int TAMANHO_MAXIMO_DA_DESCRICAO_LISTADA = 200;

  private static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

  private final FormatadorDeData datas;

  FormatadorRelatorioSemanal(ZoneId fuso) {
    this.datas = new FormatadorDeData(fuso);
  }

  String formatar(ResumoSemanal resumo, Instant inicio, Instant fim) {
    StringBuilder texto = new StringBuilder();
    texto.append("RELATÓRIO SEMANAL DE FEEDBACKS\n");
    texto.append("Período: ").append(datas.dia(inicio)).append(" a ").append(datas.dia(fim))
        .append(" (").append(datas.fuso()).append(")\n");
    texto.append("Total de avaliações: ").append(resumo.total()).append("\n");

    if (resumo.vazio()) {
      texto.append("\nNenhuma avaliação recebida no período.\n");
      return texto.toString();
    }

    texto.append(String.format(BRASIL, "Média das notas: %.2f\n", resumo.mediaDasNotas()));

    texto.append("\nQUANTIDADE POR URGÊNCIA\n");
    resumo.quantidadePorUrgencia().forEach((urgencia, quantidade) -> texto
        .append("- ").append(urgencia).append(": ").append(quantidade).append("\n"));

    texto.append("\nQUANTIDADE POR DIA\n");
    resumo.quantidadePorDia().forEach((dia, quantidade) -> texto
        .append("- ").append(datas.dia(dia)).append(": ").append(quantidade).append("\n"));

    List<Avaliacao> todas = resumo.avaliacoes();
    List<Avaliacao> listadas = todas.size() > MAXIMO_DE_AVALIACOES_LISTADAS
        ? todas.subList(todas.size() - MAXIMO_DE_AVALIACOES_LISTADAS, todas.size())
        : todas;

    texto.append("\nAVALIAÇÕES");
    if (listadas.size() < todas.size()) {
      texto.append(" (as ").append(listadas.size()).append(" mais recentes de ").append(todas.size()).append(")");
    }
    texto.append("\n");
    for (Avaliacao avaliacao : listadas) {
      texto.append("- [").append(avaliacao.urgencia()).append("] ")
          .append(datas.diaEHora(avaliacao.dataEnvio()))
          .append(" | nota ").append(avaliacao.nota().valor())
          .append(" | ").append(resumida(avaliacao.descricao())).append("\n");
    }
    return texto.toString();
  }

  private static String resumida(String descricao) {
    return descricao.length() <= TAMANHO_MAXIMO_DA_DESCRICAO_LISTADA
        ? descricao
        : descricao.substring(0, TAMANHO_MAXIMO_DA_DESCRICAO_LISTADA) + "…";
  }
}
