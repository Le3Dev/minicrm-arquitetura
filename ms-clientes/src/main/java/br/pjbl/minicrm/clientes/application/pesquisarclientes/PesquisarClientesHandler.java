package br.pjbl.minicrm.clientes.application.pesquisarclientes;

import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.FiltroClientes;
import br.pjbl.minicrm.clientes.domain.Pagina;
import br.pjbl.minicrm.clientes.domain.StatusCliente;
import br.pjbl.minicrm.clientes.domain.ValidacaoException;

/** RF04 + RF18: busca por nome ou empresa, filtros por status e responsável, 20 por página. Excluídos nunca aparecem. */
public class PesquisarClientesHandler {

    private final ClienteRepository repositorio;

    public PesquisarClientesHandler(ClienteRepository repositorio) {
        this.repositorio = repositorio;
    }

    public Pagina<ClienteView> executar(PesquisarClientesQuery q) {
        StatusCliente status = null;
        if (q.status() != null && !q.status().isBlank()) {
            status = StatusCliente.deTexto(q.status())
                    .orElseThrow(() -> new ValidacaoException("status: deve ser Prospect, Ativo ou Inativo"));
        }
        FiltroClientes filtro = new FiltroClientes(q.q(), status, q.responsaveis(), q.ids(),
                q.pagina() == null ? 1 : q.pagina(),
                q.porPagina() == null ? FiltroClientes.POR_PAGINA_PADRAO : q.porPagina());
        return repositorio.pesquisar(filtro).mapear(ClienteView::de);
    }
}
