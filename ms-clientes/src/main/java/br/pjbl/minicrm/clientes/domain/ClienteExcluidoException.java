package br.pjbl.minicrm.clientes.domain;

/** Operação não permitida em cliente já excluído (exclusão lógica). */
public class ClienteExcluidoException extends DomainException {

    public ClienteExcluidoException(String clienteId) {
        super("O cliente " + clienteId + " foi excluído e não aceita alterações");
    }

    @Override
    public String codigo() {
        return "CLIENTE_EXCLUIDO";
    }
}
