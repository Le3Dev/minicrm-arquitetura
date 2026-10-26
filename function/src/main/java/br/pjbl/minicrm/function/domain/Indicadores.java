package br.pjbl.minicrm.function.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Indicadores do dashboard do Mini CRM (RF11, RF19, RF29, RF31). */
public record Indicadores(Clientes clientes, Oportunidades oportunidades, List<ItemRanking> ranking) {

    public record Clientes(int total, Map<String, Integer> porStatus) {
    }

    public record Oportunidades(int total, BigDecimal valorEmNegociacao, BigDecimal valorGanho, int ganhas, int perdidas,
                                BigDecimal taxaConversao, List<EtapaResumo> porEtapa, Map<String, Integer> motivosPerda) {
    }

    public record EtapaResumo(String etapa, int quantidade, BigDecimal valorTotal) {
    }

    public record ItemRanking(String responsavelId, BigDecimal valorGanho, int oportunidadesGanhas, int oportunidadesPerdidas) {
    }
}
