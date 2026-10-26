package br.pjbl.minicrm.clientes.application.atualizarcliente;

/** PATCH: campo {@code null} = não alterar. */
public record AtualizarClienteCommand(String id, String nome, String empresa, String email, String telefone, String responsavelId, String status) {
}
