package br.pjbl.minicrm.clientes.application.registrarinteracao;

/** {@code data} opcional (ISO-8601); sem ela, vale o momento atual. */
public record RegistrarInteracaoCommand(String clienteId, String tipo, String obs, String autorId, java.time.Instant data) {
}
