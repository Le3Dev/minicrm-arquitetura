package br.pjbl.minicrm.function.domain;

import java.math.BigDecimal;

/** Contrato do evento publicado pelo MS Oportunidades na fila oportunidade-ganha (ver docs/asyncapi.yaml). */
public record OportunidadeGanhaEvento(String eventId, String type, String occurredAt, Dados data) {

    public static final String TIPO = "OportunidadeGanha";

    public record Dados(String oportunidadeId, String clienteId, BigDecimal valor, String responsavelId) {
    }

    /** Valida o evento já desserializado. */
    public OportunidadeGanhaEvento validar() {
        if (eventId == null || eventId.isBlank()) {
            throw new EntradaInvalidaException("eventId é obrigatório");
        }
        if (!TIPO.equals(type)) {
            throw new EntradaInvalidaException("Tipo de evento inesperado: " + type);
        }
        if (data == null || data.clienteId() == null || data.clienteId().isBlank()) {
            throw new EntradaInvalidaException("data.clienteId é obrigatório");
        }
        if (data.oportunidadeId() == null || data.oportunidadeId().isBlank()) {
            throw new EntradaInvalidaException("data.oportunidadeId é obrigatório");
        }
        return this;
    }
}
