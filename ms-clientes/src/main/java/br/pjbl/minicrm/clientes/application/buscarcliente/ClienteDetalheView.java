package br.pjbl.minicrm.clientes.application.buscarcliente;

import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.application.common.InteracaoView;

import java.util.List;

/** RF06: o cliente com o histórico de interações, da mais recente para a mais antiga. */
public record ClienteDetalheView(ClienteView cliente, List<InteracaoView> interacoes) {
}
