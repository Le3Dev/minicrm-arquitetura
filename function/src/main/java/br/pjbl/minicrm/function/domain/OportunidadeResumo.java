package br.pjbl.minicrm.function.domain;

import java.math.BigDecimal;

/** Campos de oportunidade que a Function usa. {@code resultado}: "ganho", "perdido" ou null. */
public record OportunidadeResumo(String id, String clienteId, String etapa, String resultado, BigDecimal valor,
                                 String responsavelId, String motivoPerda) {

    public boolean fechada() {
        return Etapa.deTexto(etapa).orElse(null) == Etapa.FECHADO;
    }

    public boolean ganha() {
        return fechada() && "ganho".equalsIgnoreCase(resultado);
    }

    public boolean perdida() {
        return fechada() && "perdido".equalsIgnoreCase(resultado);
    }
}
