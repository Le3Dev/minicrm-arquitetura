package br.pjbl.minicrm.function.domain;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Optional;

/** RF08: etapas do funil de vendas do Mini CRM. */
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

    /** Aceita "Prospecção", "prospeccao" ou "PROSPECCAO". */
    public static Optional<Etapa> deTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String chave = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return Arrays.stream(values()).filter(e -> e.name().equalsIgnoreCase(chave)).findFirst();
    }
}
