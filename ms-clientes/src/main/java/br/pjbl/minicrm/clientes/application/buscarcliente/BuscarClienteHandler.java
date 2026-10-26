package br.pjbl.minicrm.clientes.application.buscarcliente;

import br.pjbl.minicrm.clientes.application.common.Buscar;
import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.application.common.InteracaoView;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.Interacao;

import java.util.Comparator;

/** RF06: detalhe do cliente. Cliente excluído continua consultável (histórico preservado). */
public class BuscarClienteHandler {

    private final ClienteRepository repositorio;

    public BuscarClienteHandler(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public ClienteDetalheView executar(BuscarClienteQuery q) {
        Cliente cliente = Buscar.ouFalhar(repositorio, q.id());
        var interacoes = cliente.interacoes().stream()
                .sorted(Comparator.comparing(Interacao::data).reversed())
                .map(InteracaoView::de)
                .toList();
        return new ClienteDetalheView(ClienteView.de(cliente), interacoes);
    }
}
