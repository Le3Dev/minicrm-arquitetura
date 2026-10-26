package br.pjbl.minicrm.clientes.domain;

/**
 * Alteração parcial (PATCH): campo {@code null} = não mexer.
 * {@code responsavelId} transfere o cliente de vendedor (RF26).
 */
public record AlteracaoCliente(String nome, String empresa, String email, String telefone, String responsavelId, String status) {

    public boolean vazia() {
        return nome == null && empresa == null && email == null && telefone == null && responsavelId == null && status == null;
    }
}
