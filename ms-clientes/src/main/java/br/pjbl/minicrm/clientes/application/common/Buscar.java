package br.pjbl.minicrm.clientes.application.common;

import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteNaoEncontradoException;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;

public final class Buscar {

    private Buscar() {
    }

    public static Cliente ouFalhar(ClienteRepository repositorio, String id) {
        return repositorio.buscarPorId(id).orElseThrow(() -> new ClienteNaoEncontradoException(id));
    }
}
