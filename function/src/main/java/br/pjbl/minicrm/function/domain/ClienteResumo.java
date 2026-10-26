package br.pjbl.minicrm.function.domain;

/** Campos de cliente que a Function usa (o BFF manda o objeto completo; o resto é ignorado). */
public record ClienteResumo(String id, String status, String responsavelId) {
}
