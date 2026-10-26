package br.pjbl.minicrm.clientes.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** RF05: registro de uma ligação, e-mail ou reunião com o cliente. Imutável. */
public record Interacao(String id, TipoInteracao tipo, String obs, Instant data, String autorId) {

    public Interacao {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tipo, "tipo");
        Objects.requireNonNull(obs, "obs");
        Objects.requireNonNull(data, "data");
        Objects.requireNonNull(autorId, "autorId");
    }

    /** Cria validando as regras do Mini CRM: tipo do domínio, observação com ao menos 3 caracteres. */
    public static Interacao nova(String tipo, String obs, String autorId, Instant data) {
        List<String> erros = new ArrayList<>();
        TipoInteracao tipoValido = TipoInteracao.deTexto(tipo).orElse(null);
        if (tipoValido == null) {
            erros.add("tipo: deve ser Ligação, E-mail ou Reunião");
        }
        String obsLimpa = obs == null ? "" : obs.trim();
        if (obsLimpa.length() < 3) {
            erros.add("obs: deve ter ao menos 3 caracteres");
        } else if (obsLimpa.length() > 2000) {
            erros.add("obs: deve ter no máximo 2000 caracteres");
        }
        String autor = autorId == null ? "" : autorId.trim();
        if (autor.isEmpty()) {
            erros.add("autorId: obrigatório");
        }
        if (!erros.isEmpty()) {
            throw new ValidacaoException("Interação inválida", erros);
        }
        return new Interacao(UUID.randomUUID().toString(), tipoValido, obsLimpa, data, autor);
    }
}
