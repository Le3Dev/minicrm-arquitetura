package br.pjbl.minicrm.clientes.api.clientes;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** Corpos de requisição da API de clientes. As regras de negócio completas ficam no domínio. */
public final class ClienteRequests {

    private ClienteRequests() {
    }

    @Schema(name = "NovoCliente")
    public record NovoCliente(
            @Schema(example = "Mariana Souza") @NotBlank @Size(min = 3, max = 120) String nome,
            @Schema(example = "Acme Ltda") @NotBlank @Size(min = 2, max = 120) String empresa,
            @Schema(example = "mariana@acme.com.br") @NotBlank @Email String email,
            @Schema(example = "(41) 99999-0000") @Size(max = 30) String telefone,
            @Schema(example = "u2", description = "Vendedor responsável (RF26)") @NotBlank String responsavelId) {
    }

    @Schema(name = "AlteracaoCliente", description = "Envie só os campos que mudam. responsavelId transfere o cliente (RF26).")
    public record Alteracao(
            @Schema(example = "Mariana S. Souza") @Size(min = 3, max = 120) String nome,
            @Schema(example = "Acme S.A.") @Size(min = 2, max = 120) String empresa,
            @Schema(example = "mariana@acme.com") @Email String email,
            @Size(max = 30) String telefone,
            @Schema(example = "u5") String responsavelId,
            @Schema(example = "Ativo", allowableValues = {"Prospect", "Ativo"}) String status) {
    }

    @Schema(name = "NovaInteracao")
    public record NovaInteracao(
            @Schema(example = "Reunião", allowableValues = {"Ligação", "E-mail", "Reunião"}) @NotBlank String tipo,
            @Schema(example = "Apresentei a proposta; cliente pediu desconto.") @NotBlank @Size(min = 3, max = 2000) String obs,
            @Schema(example = "u2") @NotBlank String autorId,
            @Schema(description = "Opcional; padrão = agora") @PastOrPresent Instant data) {
    }

    @Schema(name = "Ativacao", description = "Enviado pela Azure Function ao consumir o evento OportunidadeGanha.")
    public record Ativacao(
            @Schema(example = "8d2f1c7a-3b4e-4f5a-9c6d-1e2f3a4b5c6d", description = "eventId do OportunidadeGanha (idempotência)")
            @NotBlank @Size(max = 100) String eventoId,
            @Schema(example = "123") String oportunidadeId) {
    }
}
