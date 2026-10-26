package br.pjbl.minicrm.clientes.domain;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Optional;

/** RF30: o cliente é Prospect, Ativo ou Inativo. */
public enum StatusCliente {
    PROSPECT("Prospect"),
    ATIVO("Ativo"),
    INATIVO("Inativo");

    private final String rotulo;

    StatusCliente(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Aceita "Ativo", "ativo" ou "ATIVO". */
    public static Optional<StatusCliente> deTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String chave = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return Arrays.stream(values()).filter(s -> s.rotulo.equalsIgnoreCase(chave) || s.name().equalsIgnoreCase(chave)).findFirst();
    }
}
