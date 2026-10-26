package br.pjbl.minicrm.function.infrastructure;

import java.util.Map;

/** Lê as configurações da Function App (variáveis de ambiente). */
public record Configuracao(String msClientesUrl) {

    public static Configuracao doAmbiente() {
        return de(System.getenv());
    }

    public static Configuracao de(Map<String, String> env) {
        String url = env.get("MS_CLIENTES_URL");
        return new Configuracao(url == null || url.isBlank() ? null : url.trim().replaceAll("/+$", ""));
    }
}
