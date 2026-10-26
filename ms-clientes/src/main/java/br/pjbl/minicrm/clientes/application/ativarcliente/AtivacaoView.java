package br.pjbl.minicrm.clientes.application.ativarcliente;

import br.pjbl.minicrm.clientes.application.common.ClienteView;

/** {@code resultado}: ATIVADO, JA_ATIVO, REPETIDO ou IGNORADO_EXCLUIDO. */
public record AtivacaoView(String resultado, ClienteView cliente) {
}
