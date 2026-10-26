package br.pjbl.minicrm.clientes.api.comum;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Formato de erro: lista completa de problemas, como no Mini CRM original. */
@Schema(name = "Erro")
public record ErroResponse(
        @Schema(example = "400") int status,
        @Schema(example = "VALIDACAO") String codigo,
        @Schema(example = "Cliente inválido") String erro,
        @Schema(example = "[\"email: formato inválido\"]") List<String> erros) {
}
