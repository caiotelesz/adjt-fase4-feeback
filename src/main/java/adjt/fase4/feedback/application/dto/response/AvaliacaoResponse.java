package adjt.fase4.feedback.application.dto.response;

import adjt.fase4.feedback.common.model.Urgencia;

public record AvaliacaoResponse(String id, String descricao, Integer nota, Urgencia urgencia, String dataEnvio) {}
