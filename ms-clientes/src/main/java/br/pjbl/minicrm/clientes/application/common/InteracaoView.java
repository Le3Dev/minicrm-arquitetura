package br.pjbl.minicrm.clientes.application.common;

import br.pjbl.minicrm.clientes.domain.Interacao;

import java.time.Instant;

public record InteracaoView(String id, String tipo, String obs, Instant data, String autorId) {

    public static InteracaoView de(Interacao i) {
        return new InteracaoView(i.id(), i.tipo().rotulo(), i.obs(), i.data(), i.autorId());
    }
}
