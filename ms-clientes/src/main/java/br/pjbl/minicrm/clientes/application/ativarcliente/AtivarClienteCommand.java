package br.pjbl.minicrm.clientes.application.ativarcliente;

/** Vem do evento OportunidadeGanha, via Azure Function. */
public record AtivarClienteCommand(String clienteId, String eventoId, String oportunidadeId) {
}
