package br.pjbl.minicrm.clientes.domain;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Optional;

/** RF05: ligação, e-mail ou reunião. */
public enum TipoInteracao {
    LIGACAO("Ligação"),
    EMAIL("E-mail"),
    REUNIAO("Reunião");

    private final String rotulo;

    TipoInteracao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    /** Aceita "Ligação", "ligacao", "E-mail", "email", "REUNIAO"... */
    public static Optional<TipoInteracao> deTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String chave = semAcento(texto).replace("-", "").trim();
        return Arrays.stream(values())
                .filter(t -> semAcento(t.rotulo).replace("-", "").equalsIgnoreCase(chave) || t.name().equalsIgnoreCase(chave))
                .findFirst();
    }

    private static String semAcento(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
