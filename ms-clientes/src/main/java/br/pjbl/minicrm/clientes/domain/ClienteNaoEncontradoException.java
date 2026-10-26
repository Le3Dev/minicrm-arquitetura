package br.pjbl.minicrm.clientes.domain;

public class ClienteNaoEncontradoException extends DomainException {

    public ClienteNaoEncontradoException(String clienteId) {
        super("Cliente " + clienteId + " não encontrado");
    }

    @Override
    public String codigo() {
        return "NAO_ENCONTRADO";
    }
}
