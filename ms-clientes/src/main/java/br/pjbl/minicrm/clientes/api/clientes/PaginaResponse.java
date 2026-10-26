package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.domain.Pagina;

import java.util.List;

/** Mesmo formato de resposta paginada do Mini CRM original. */
public record PaginaResponse(List<ClienteView> itens, Paginacao paginacao, Filtros filtros) {

    public record Paginacao(int pagina, int porPagina, long total, int totalPaginas) {
    }

    public record Filtros(String q, String status, List<String> responsaveis) {
    }

    public static PaginaResponse de(Pagina<ClienteView> p, String q, String status, List<String> responsaveis) {
        return new PaginaResponse(p.itens(), new Paginacao(p.pagina(), p.porPagina(), p.total(), p.totalPaginas()),
                new Filtros(q == null ? "" : q, status == null ? "" : status, responsaveis));
    }
}
