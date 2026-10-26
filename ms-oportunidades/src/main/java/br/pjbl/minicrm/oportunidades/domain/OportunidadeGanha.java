package br.pjbl.minicrm.oportunidades.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Evento de domínio: a oportunidade foi fechada como ganha.
 * Vira a mensagem da fila oportunidade-ganha (contrato em function/src/main/resources/docs/asyncapi.yaml).
 */
public record OportunidadeGanha(String eventId, long oportunidadeId, String clienteId, BigDecimal valor, String responsavelId, Instant ocorridoEm) {
}
