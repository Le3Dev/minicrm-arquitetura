package br.pjbl.minicrm.function.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculadoraDeIndicadoresTest {

    private static OportunidadeResumo op(String id, String etapa, String resultado, String valor, String resp, String motivo) {
        return new OportunidadeResumo(id, "c1", etapa, resultado, new BigDecimal(valor), resp, motivo);
    }

    private final List<ClienteResumo> clientes = List.of(
            new ClienteResumo("c1", "Ativo", "u2"),
            new ClienteResumo("c2", "Prospect", "u5"),
            new ClienteResumo("c3", "prospect", "u5"));

    private final List<OportunidadeResumo> oportunidades = List.of(
            op("1", "Fechado", "ganho", "18500", "u2", null),
            op("2", "Proposta", null, "7200.50", "u5", null),
            op("3", "Fechado", "perdido", "3000", "u5", "Preço"),
            op("4", "prospeccao", null, "1000", "u2", null),
            op("5", "Fechado", "ganho", "2000", "u5", null),
            op("6", "Fechado", "perdido", "500", "u2", null));

    private final Indicadores indicadores = CalculadoraDeIndicadores.calcular(clientes, oportunidades);

    @Test
    void contaClientesPorStatusComTodosOsStatus() {
        assertThat(indicadores.clientes().total()).isEqualTo(3);
        assertThat(indicadores.clientes().porStatus()).containsExactly(
                java.util.Map.entry("Prospect", 2), java.util.Map.entry("Ativo", 1), java.util.Map.entry("Inativo", 0));
    }

    @Test
    void somaValorEmNegociacaoSoDasAbertasEValorGanho() {
        assertThat(indicadores.oportunidades().valorEmNegociacao()).isEqualByComparingTo("8200.50");
        assertThat(indicadores.oportunidades().valorGanho()).isEqualByComparingTo("20500");
        assertThat(indicadores.oportunidades().ganhas()).isEqualTo(2);
        assertThat(indicadores.oportunidades().perdidas()).isEqualTo(2);
        assertThat(indicadores.oportunidades().taxaConversao()).isEqualByComparingTo("0.5");
    }

    @Test
    void resumeAsQuatroEtapasNaOrdemDoFunil() {
        assertThat(indicadores.oportunidades().porEtapa())
                .extracting(Indicadores.EtapaResumo::etapa, Indicadores.EtapaResumo::quantidade)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Prospecção", 1),
                        org.assertj.core.groups.Tuple.tuple("Contato", 0),
                        org.assertj.core.groups.Tuple.tuple("Proposta", 1),
                        org.assertj.core.groups.Tuple.tuple("Fechado", 4));
    }

    @Test
    void contaMotivosDePerda() {
        assertThat(indicadores.oportunidades().motivosPerda()).containsEntry("Preço", 1).containsEntry("Não informado", 1);
    }

    @Test
    void rankingPorValorGanho() {
        assertThat(indicadores.ranking()).extracting(Indicadores.ItemRanking::responsavelId).containsExactly("u2", "u5");
        assertThat(indicadores.ranking().get(0).valorGanho()).isEqualByComparingTo("18500");
        assertThat(indicadores.ranking().get(0).oportunidadesPerdidas()).isEqualTo(1);
    }

    @Test
    void semDadosDevolveZeros() {
        Indicadores vazio = CalculadoraDeIndicadores.calcular(List.of(), List.of());
        assertThat(vazio.oportunidades().taxaConversao()).isEqualByComparingTo("0");
        assertThat(vazio.ranking()).isEmpty();
    }

    @Test
    void eventoOportunidadeGanhaValidaCamposObrigatorios() {
        var dados = new OportunidadeGanhaEvento.Dados("123", "c1", new BigDecimal("10"), "u2");
        assertThat(new OportunidadeGanhaEvento("ev", "OportunidadeGanha", "2026-11-12T15:30:00Z", dados).validar()).isNotNull();
        assertThatThrownBy(() -> new OportunidadeGanhaEvento("", "OportunidadeGanha", null, dados).validar()).isInstanceOf(EntradaInvalidaException.class);
        assertThatThrownBy(() -> new OportunidadeGanhaEvento("ev", "PedidoCriado", null, dados).validar()).isInstanceOf(EntradaInvalidaException.class);
        assertThatThrownBy(() -> new OportunidadeGanhaEvento("ev", "OportunidadeGanha", null,
                new OportunidadeGanhaEvento.Dados("123", null, null, null)).validar()).isInstanceOf(EntradaInvalidaException.class);
        assertThat(Etapa.deTexto("PROSPECÇÃO")).contains(Etapa.PROSPECCAO);
    }
}
