package br.pjbl.minicrm.clientes.domain;

import java.util.List;
import java.util.function.Function;

/** Uma página de resultados (RF18: 20 por página por padrão). */
public record Pagina<T>(List<T> itens, int pagina, int porPagina, long total) {

    public Pagina {
        itens = List.copyOf(itens);
    }

    public int totalPaginas() {
        return total == 0 ? 0 : (int) ((total + porPagina - 1) / porPagina);
    }

    public <R> Pagina<R> mapear(Function<T, R> funcao) {
        return new Pagina<>(itens.stream().map(funcao).toList(), pagina, porPagina, total);
    }
}
