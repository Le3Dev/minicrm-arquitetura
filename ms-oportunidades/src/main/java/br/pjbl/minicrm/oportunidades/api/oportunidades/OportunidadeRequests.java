package br.pjbl.minicrm.oportunidades.api.oportunidades;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class OportunidadeRequests {

    private OportunidadeRequests() {
    }

    @Schema(name = "NovaOportunidade")
    public record Nova(
            @Schema(example = "3f6c2a1e-8b4d-4c7a-9f10-2b5e6d7c8a90", description = "Id do cliente no MS Clientes") @NotBlank String clienteId,
            @Schema(example = "Plano anual — 20 licenças") @NotBlank @Size(min = 3, max = 160) String titulo,
            @Schema(example = "18500.00") @NotNull @DecimalMin(value = "0.01") BigDecimal valor,
            @Schema(example = "2026-12-15") LocalDate dataPrevista,
            @Schema(example = "u2") @NotBlank String responsavelId,
            @Schema(example = "Prospecção", allowableValues = {"Prospecção", "Contato", "Proposta"}) String etapa) {
    }

    @Schema(name = "Movimentacao", description = "RF09: fechar exige resultado; perdido exige motivo; sair do Fechado exige reabrir = true.")
    public record Movimentacao(
            @Schema(example = "Fechado", allowableValues = {"Prospecção", "Contato", "Proposta", "Fechado"}) @NotBlank String etapa,
            @Schema(example = "ganho", allowableValues = {"ganho", "perdido"}) String resultado,
            @Schema(example = "Preço", allowableValues = {"Preço", "Concorrente", "Sem verba", "Sem resposta"}) String motivoPerda,
            @Schema(example = "false") Boolean reabrir) {
    }
}
