package br.pjbl.minicrm.clientes.application.common;

import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.Interacao;

import java.time.Instant;
import java.util.Comparator;

/** Saída dos casos de uso: o que a API devolve em JSON. */
public record ClienteView(
        String id, String nome, String empresa, String email, String telefone, String status, String responsavelId,
        boolean excluido, int totalInteracoes, Instant ultimaInteracaoEm, Instant criadoEm, Instant atualizadoEm) {

    public static ClienteView de(Cliente c) {
        Instant ultima = c.interacoes().stream().map(Interacao::data).max(Comparator.naturalOrder()).orElse(null);
        return new ClienteView(c.id(), c.nome(), c.empresa(), c.email(), c.telefone(), c.status().rotulo(), c.responsavelId(),
                c.excluido(), c.interacoes().size(), ultima, c.criadoEm(), c.atualizadoEm());
    }
}
