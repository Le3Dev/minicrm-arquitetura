package br.pjbl.minicrm.oportunidades.domain;

import java.util.Arrays;
import java.util.Optional;

/** Resultado de uma oportunidade fechada. */
public enum Resultado {
    GANHO("ganho"),
    PERDIDO("perdido");

    private final String rotulo;

    Resultado(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    public static Optional<Resultado> deTexto(String texto) {
        return texto == null ? Optional.empty() : Arrays.stream(values()).filter(r -> r.rotulo.equalsIgnoreCase(texto.trim())).findFirst();
    }
}
