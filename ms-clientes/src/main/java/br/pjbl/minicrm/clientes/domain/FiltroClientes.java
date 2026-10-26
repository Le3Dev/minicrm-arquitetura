package br.pjbl.minicrm.clientes.domain;

import java.util.List;

/**
 * Filtros da pesquisa de clientes (RF04, RF18).
 *
 * @param texto          trecho de nome ou empresa, sem diferenciar maiúsculas e acentos
 * @param status         status exato, ou {@code null} para todos
 * @param responsaveis   restringe a estes responsáveis (o BFF calcula o escopo do usuário, RF15); vazio = todos
 * @param ids            restringe a estes ids (o BFF usa para montar telas); vazio = todos
 * @param pagina         começa em 1
 * @param porPagina      itens por página
 */
public record FiltroClientes(String texto, StatusCliente status, List<String> responsaveis, List<String> ids, int pagina, int porPagina) {

    public static final int POR_PAGINA_PADRAO = 20;
    public static final int POR_PAGINA_MAXIMO = 200;

    public FiltroClientes {
        texto = texto == null || texto.isBlank() ? null : texto.trim();
        responsaveis = responsaveis == null ? List.of() : List.copyOf(responsaveis);
        ids = ids == null ? List.of() : List.copyOf(ids);
        pagina = Math.max(1, pagina);
        porPagina = porPagina <= 0 ? POR_PAGINA_PADRAO : Math.min(porPagina, POR_PAGINA_MAXIMO);
    }

    public int deslocamento() {
        return (pagina - 1) * porPagina;
    }
}
