package br.pjbl.minicrm.clientes.domain;

/** Dados que o usuário informa ao cadastrar um cliente. */
public record DadosCliente(String nome, String empresa, String email, String telefone, String responsavelId) {
}
