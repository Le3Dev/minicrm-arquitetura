package br.pjbl.minicrm.clientes.application.cadastrarcliente;

public record CadastrarClienteCommand(String nome, String empresa, String email, String telefone, String responsavelId) {
}
