package br.pjbl.minicrm.function.application.calcularindicadores;

import br.pjbl.minicrm.function.domain.CalculadoraDeIndicadores;
import br.pjbl.minicrm.function.domain.ClienteResumo;
import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import br.pjbl.minicrm.function.domain.Indicadores;
import br.pjbl.minicrm.function.domain.OportunidadeResumo;

import java.math.BigDecimal;
import java.util.List;

/** Caso de uso da rota HTTP: valida o que o BFF mandou e calcula os indicadores. */
public class CalcularIndicadoresHandler {

    public Indicadores executar(CalcularIndicadoresCommand comando) {
        if (comando == null || comando.clientes() == null || comando.oportunidades() == null) {
            throw new EntradaInvalidaException("Envie { \"clientes\": [...], \"oportunidades\": [...] }");
        }
        List<ClienteResumo> clientes = comando.clientes();
        for (int i = 0; i < clientes.size(); i++) {
            ClienteResumo c = clientes.get(i);
            if (c == null || c.id() == null || c.status() == null) {
                throw new EntradaInvalidaException("clientes[" + i + "] precisa de id e status");
            }
        }
        List<OportunidadeResumo> oportunidades = comando.oportunidades();
        for (int i = 0; i < oportunidades.size(); i++) {
            OportunidadeResumo o = oportunidades.get(i);
            if (o == null || o.id() == null || o.etapa() == null || o.valor() == null) {
                throw new EntradaInvalidaException("oportunidades[" + i + "] precisa de id, etapa e valor");
            }
            if (o.valor().compareTo(BigDecimal.ZERO) < 0) {
                throw new EntradaInvalidaException("oportunidades[" + i + "].valor não pode ser negativo");
            }
        }
        return CalculadoraDeIndicadores.calcular(clientes, oportunidades);
    }
}
