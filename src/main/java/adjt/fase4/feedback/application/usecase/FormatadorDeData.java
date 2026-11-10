package adjt.fase4.feedback.application.usecase;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/** Apresenta datas no fuso horário dos administradores. */
final class FormatadorDeData {

  private static final DateTimeFormatter DIA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter DIA_E_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  private final ZoneId fuso;

  FormatadorDeData(ZoneId fuso) {
    this.fuso = fuso;
  }

  String dia(LocalDate dia) {
    return DIA.format(dia);
  }

  String dia(Instant instante) {
    return DIA.format(instante.atZone(fuso));
  }

  String diaEHora(Instant instante) {
    return DIA_E_HORA.format(instante.atZone(fuso));
  }

  String fuso() {
    return fuso.getId();
  }
}
