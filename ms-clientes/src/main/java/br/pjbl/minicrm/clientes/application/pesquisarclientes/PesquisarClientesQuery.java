package br.pjbl.minicrm.clientes.application.pesquisarclientes;

import java.util.List;

/** {@code status} chega como texto ("Ativo"); vazio = todos. */
public record PesquisarClientesQuery(String q, String status, List<String> responsaveis, List<String> ids, Integer pagina, Integer porPagina) {
}
