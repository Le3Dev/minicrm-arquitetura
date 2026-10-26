package br.pjbl.minicrm.function.application.calcularindicadores;

import br.pjbl.minicrm.function.domain.ClienteResumo;
import br.pjbl.minicrm.function.domain.OportunidadeResumo;

import java.util.List;

/** Corpo que o BFF envia em POST /api/indicadores. */
public record CalcularIndicadoresCommand(List<ClienteResumo> clientes, List<OportunidadeResumo> oportunidades) {
}
