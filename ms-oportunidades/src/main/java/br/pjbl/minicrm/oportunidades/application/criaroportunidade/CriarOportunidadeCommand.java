package br.pjbl.minicrm.oportunidades.application.criaroportunidade;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CriarOportunidadeCommand(String clienteId, String titulo, BigDecimal valor, LocalDate dataPrevista, String responsavelId, String etapa) {
}
