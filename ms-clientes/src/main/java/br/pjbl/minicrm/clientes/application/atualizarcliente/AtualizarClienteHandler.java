package br.pjbl.minicrm.clientes.application.atualizarcliente;

import br.pjbl.minicrm.clientes.application.common.Buscar;
import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.application.common.ComRetentativa;
import br.pjbl.minicrm.clientes.domain.AlteracaoCliente;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;

import java.time.Clock;

/** RF03 + RF26: altera dados do cliente, inclusive transfere o responsável. */
public class AtualizarClienteHandler {

    private final ClienteRepository repositorio;
    private final Clock relogio;

    public AtualizarClienteHandler(ClienteRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public ClienteView executar(AtualizarClienteCommand c) {
        return ComRetentativa.executar(() -> {
            Cliente cliente = Buscar.ouFalhar(repositorio, c.id());
            cliente.alterar(new AlteracaoCliente(c.nome(), c.empresa(), c.email(), c.telefone(), c.responsavelId(), c.status()), relogio.instant());
            repositorio.salvar(cliente);
            return ClienteView.de(cliente);
        });
    }
}
