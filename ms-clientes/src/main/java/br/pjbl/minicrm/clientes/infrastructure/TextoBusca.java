package br.pjbl.minicrm.clientes.infrastructure;

import java.text.Normalizer;
import java.util.Locale;

/** Texto em minúsculas e sem acentos, para busca por nome ou empresa (RF04). */
public final class TextoBusca {

    private TextoBusca() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT).trim();
    }

    public static String de(String nome, String empresa) {
        return normalizar(nome) + " " + normalizar(empresa);
    }
}
