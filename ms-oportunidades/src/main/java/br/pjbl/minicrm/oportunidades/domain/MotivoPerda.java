package br.pjbl.minicrm.oportunidades.domain;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Optional;

/** RF29: lista fixa de motivos de perda. */
public enum MotivoPerda {
    PRECO("Preço"),
    CONCORRENTE("Concorrente"),
    SEM_VERBA("Sem verba"),
    SEM_RESPOSTA("Sem resposta");

    private final String rotulo;

    MotivoPerda(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    public static Optional<MotivoPerda> deTexto(String texto) {
        if (texto == null) {
            return Optional.empty();
        }
        String chave = semAcento(texto).trim();
        return Arrays.stream(values()).filter(m -> semAcento(m.rotulo).equalsIgnoreCase(chave) || m.name().equalsIgnoreCase(chave)).findFirst();
    }

    private static String semAcento(String t) {
        return Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }
}
