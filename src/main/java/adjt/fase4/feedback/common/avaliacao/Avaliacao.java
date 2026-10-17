package adjt.fase4.feedback.common.avaliacao;

import adjt.fase4.feedback.common.classify.UrgenciaClassify;
import adjt.fase4.feedback.common.model.Urgencia;

import java.time.Instant;
import java.util.UUID;

public record Avaliacao(String id, String descricao, int nota, Urgencia urgencia, String dataEnvio) {

    public static Avaliacao nova(String descricao, Integer nota) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("A descrição é obrigatória");
        }
        if (nota == null) {
            throw new IllegalArgumentException("A nota é obrigatória");
        }

        return new Avaliacao(UUID.randomUUID().toString(), descricao.trim(), nota,
                UrgenciaClassify.fromNota(nota), Instant.now().toString());
    }
}
