package adjt.fase4.feedback.common.classify;

import adjt.fase4.feedback.common.model.Urgencia;

public class UrgenciaClassify {

    public static Urgencia fromNota(int nota) {
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("A nota deve estar entre 0 e 10");
        }

        if (nota <= 3) return Urgencia.CRITICA;
        if (nota <= 6) return Urgencia.ATENCAO;

        return Urgencia.OK;
    }
}
