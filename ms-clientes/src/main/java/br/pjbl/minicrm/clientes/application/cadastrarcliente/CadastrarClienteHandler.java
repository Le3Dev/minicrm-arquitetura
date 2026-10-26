package br.pjbl.minicrm.clientes.application.cadastrarcliente;

import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.DadosCliente;

import java.time.Clock;

/** RF03: cadastra um cliente, que nasce como Prospect. */
public class CadastrarClienteHandler {

    private final ClienteRepository repositorio;
    private final Clock relogio;

    public CadastrarClienteHandler(ClienteRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public ClienteView executar(CadastrarClienteCommand c) {
        Cliente cliente = Cliente.cadastrar(new DadosCliente(c.nome(), c.empresa(), c.email(), c.telefone(), c.responsavelId()), relogio.instant());
        repositorio.salvar(cliente);
        return ClienteView.de(cliente);
    }
}
