package br.pjbl.minicrm.oportunidades.domain;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Optional;

/** RF08: Prospecção → Contato → Proposta → Fechado. */
public enum Etapa {
    PROSPECCAO("Prospecção"),
    CONTATO("Contato"),
    PROPOSTA("Proposta"),
    FECHADO("Fechado");

    private final String rotulo;

    Etapa(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    public static Optional<Etapa> deTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String chave = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(chave)).findFirst();
    }
}
