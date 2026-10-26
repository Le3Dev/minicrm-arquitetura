package br.pjbl.minicrm.function.application;

import br.pjbl.minicrm.function.application.ativarcliente.AtivarClienteHandler;
import br.pjbl.minicrm.function.application.ativarcliente.ResultadoAtivacao;
import br.pjbl.minicrm.function.application.calcularindicadores.CalcularIndicadoresCommand;
import br.pjbl.minicrm.function.application.calcularindicadores.CalcularIndicadoresHandler;
import br.pjbl.minicrm.function.domain.ClienteResumo;
import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import br.pjbl.minicrm.function.domain.ErroTransitorioException;
import br.pjbl.minicrm.function.domain.OportunidadeGanhaEvento;
import br.pjbl.minicrm.function.domain.OportunidadeResumo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoTest {

    private static final OportunidadeGanhaEvento EVENTO = new OportunidadeGanhaEvento("ev-1", "OportunidadeGanha", "2026-11-12T15:30:00Z",
            new OportunidadeGanhaEvento.Dados("123", "c1", new BigDecimal("18500"), "u2"));

    @Test
    void ativarChamaOMsClientesComEventIdEOportunidade() {
        List<String> chamadas = new ArrayList<>();
        var handler = new AtivarClienteHandler((cliente, evento, oportunidade) -> {
            chamadas.add(cliente + "|" + evento + "|" + oportunidade);
            return ResultadoAtivacao.ATIVADO;
        });

        var resultado = handler.executar(EVENTO);

        assertThat(chamadas).containsExactly("c1|ev-1|123");
        assertThat(resultado.resultado()).isEqualTo(ResultadoAtivacao.ATIVADO);
        assertThat(resultado.pendente()).isFalse();
    }

    @Test
    void clienteInexistenteViraPendenciaSemLancar() {
        var resultado = new AtivarClienteHandler((c, e, o) -> ResultadoAtivacao.NAO_ENCONTRADO).executar(EVENTO);
        assertThat(resultado.pendente()).isTrue();
    }

    @Test
    void falhaTemporariaSobeParaOServiceBusTentarDeNovo() {
        var handler = new AtivarClienteHandler((c, e, o) -> {
            throw new ErroTransitorioException("503");
        });
        assertThatThrownBy(() -> handler.executar(EVENTO)).isInstanceOf(ErroTransitorioException.class);
    }

    @Test
    void eventoInvalidoNaoChamaOMsClientes() {
        List<String> chamadas = new ArrayList<>();
        var handler = new AtivarClienteHandler((c, e, o) -> {
            chamadas.add(c);
            return ResultadoAtivacao.ATIVADO;
        });
        assertThatThrownBy(() -> handler.executar(new OportunidadeGanhaEvento("ev", "Outro", null, null))).isInstanceOf(EntradaInvalidaException.class);
        assertThat(chamadas).isEmpty();
    }

    @Test
    void indicadoresValidamAEntrada() {
        var handler = new CalcularIndicadoresHandler();
        var ok = handler.executar(new CalcularIndicadoresCommand(List.of(new ClienteResumo("c1", "Ativo", "u2")),
                List.of(new OportunidadeResumo("1", "c1", "Contato", null, new BigDecimal("100"), "u2", null))));
        assertThat(ok.oportunidades().valorEmNegociacao()).isEqualByComparingTo("100");

        assertThatThrownBy(() -> handler.executar(new CalcularIndicadoresCommand(null, List.of()))).isInstanceOf(EntradaInvalidaException.class);
        assertThatThrownBy(() -> handler.executar(new CalcularIndicadoresCommand(List.of(new ClienteResumo(null, "Ativo", null)), List.of())))
                .isInstanceOf(EntradaInvalidaException.class);
        assertThatThrownBy(() -> handler.executar(new CalcularIndicadoresCommand(List.of(),
                List.of(new OportunidadeResumo("1", "c1", "Contato", null, new BigDecimal("-1"), "u2", null)))))
                .isInstanceOf(EntradaInvalidaException.class);
    }
}
