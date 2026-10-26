package br.pjbl.minicrm.function.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Regra de negócio pura da Function: transforma as listas de clientes e oportunidades
 * nos indicadores do dashboard. Sem I/O, fácil de testar.
 */
public final class CalculadoraDeIndicadores {

    private static final List<String> STATUS = List.of("Prospect", "Ativo", "Inativo");

    private CalculadoraDeIndicadores() {
    }

    public static Indicadores calcular(List<ClienteResumo> clientes, List<OportunidadeResumo> oportunidades) {
        return new Indicadores(resumirClientes(clientes), resumirOportunidades(oportunidades), ranking(oportunidades));
    }

    private static Indicadores.Clientes resumirClientes(List<ClienteResumo> clientes) {
        Map<String, Integer> porStatus = new LinkedHashMap<>();
        STATUS.forEach(s -> porStatus.put(s, 0));
        for (ClienteResumo c : clientes) {
            String status = STATUS.stream().filter(s -> s.equalsIgnoreCase(c.status())).findFirst().orElse(c.status());
            porStatus.merge(status, 1, Integer::sum);
        }
        return new Indicadores.Clientes(clientes.size(), porStatus);
    }

    private static Indicadores.Oportunidades resumirOportunidades(List<OportunidadeResumo> oportunidades) {
        BigDecimal emNegociacao = soma(oportunidades.stream().filter(o -> !o.fechada()).toList());
        List<OportunidadeResumo> ganhas = oportunidades.stream().filter(OportunidadeResumo::ganha).toList();
        long perdidas = oportunidades.stream().filter(OportunidadeResumo::perdida).count();
        BigDecimal taxa = ganhas.size() + perdidas == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(ganhas.size()).divide(BigDecimal.valueOf(ganhas.size() + perdidas), 4, RoundingMode.HALF_UP);

        List<Indicadores.EtapaResumo> porEtapa = new ArrayList<>();
        for (Etapa etapa : Etapa.values()) {
            List<OportunidadeResumo> daEtapa = oportunidades.stream().filter(o -> Etapa.deTexto(o.etapa()).orElse(null) == etapa).toList();
            porEtapa.add(new Indicadores.EtapaResumo(etapa.rotulo(), daEtapa.size(), soma(daEtapa)));
        }

        Map<String, Integer> motivos = new LinkedHashMap<>();
        oportunidades.stream().filter(OportunidadeResumo::perdida)
                .map(o -> o.motivoPerda() == null || o.motivoPerda().isBlank() ? "Não informado" : o.motivoPerda())
                .forEach(m -> motivos.merge(m, 1, Integer::sum));

        return new Indicadores.Oportunidades(oportunidades.size(), emNegociacao, soma(ganhas), ganhas.size(), (int) perdidas,
                taxa, porEtapa, motivos);
    }

    /** RF31: vendedores por valor ganho, depois por oportunidades ganhas. */
    private static List<Indicadores.ItemRanking> ranking(List<OportunidadeResumo> oportunidades) {
        Map<String, BigDecimal> valor = new HashMap<>();
        Map<String, Integer> ganhas = new HashMap<>();
        Map<String, Integer> perdidas = new HashMap<>();
        for (OportunidadeResumo o : oportunidades) {
            if (o.responsavelId() == null) {
                continue;
            }
            if (o.ganha()) {
                valor.merge(o.responsavelId(), valorDe(o), BigDecimal::add);
                ganhas.merge(o.responsavelId(), 1, Integer::sum);
            } else if (o.perdida()) {
                perdidas.merge(o.responsavelId(), 1, Integer::sum);
            }
        }
        return java.util.stream.Stream.concat(ganhas.keySet().stream(), perdidas.keySet().stream()).distinct()
                .map(id -> new Indicadores.ItemRanking(id, valor.getOrDefault(id, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
                        ganhas.getOrDefault(id, 0), perdidas.getOrDefault(id, 0)))
                .sorted(Comparator.comparing(Indicadores.ItemRanking::valorGanho).reversed()
                        .thenComparing(Comparator.comparingInt(Indicadores.ItemRanking::oportunidadesGanhas).reversed())
                        .thenComparing(Indicadores.ItemRanking::responsavelId))
                .toList();
    }

    private static BigDecimal soma(List<OportunidadeResumo> lista) {
        return lista.stream().map(CalculadoraDeIndicadores::valorDe).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal valorDe(OportunidadeResumo o) {
        return o.valor() == null ? BigDecimal.ZERO : o.valor();
    }
}
