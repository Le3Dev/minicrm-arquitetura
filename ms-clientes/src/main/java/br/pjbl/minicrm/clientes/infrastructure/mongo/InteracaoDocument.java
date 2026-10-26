package br.pjbl.minicrm.clientes.infrastructure.mongo;

import java.time.Instant;

/** Interação embutida no documento do cliente (o histórico vive junto do cliente). */
public record InteracaoDocument(String id, String tipo, String obs, Instant data, String autorId) {
}
