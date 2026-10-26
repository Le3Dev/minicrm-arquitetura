package br.pjbl.minicrm.function.functions;

import br.pjbl.minicrm.function.application.ativarcliente.AtivarClienteHandler;
import br.pjbl.minicrm.function.application.ativarcliente.ResultadoAtivacao;
import br.pjbl.minicrm.function.application.calcularindicadores.CalcularIndicadoresHandler;
import br.pjbl.minicrm.function.domain.ErroTransitorioException;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FunctionsTest {

    private static final String EVENTO = """
            {"eventId":"ev-1","type":"OportunidadeGanha","occurredAt":"2026-11-12T15:30:00Z",
             "data":{"oportunidadeId":"123","clienteId":"c1","valor":18500,"responsavelId":"u2"}}
            """;

    @Test
    void indicadoresResponde200ComJson() {
        var funcao = new IndicadoresFunction(new CalcularIndicadoresHandler());
        String corpo = """
                {"clientes":[{"id":"c1","status":"Ativo","responsavelId":"u2","nome":"ignorado"}],
                 "oportunidades":[{"id":"1","clienteId":"c1","etapa":"Fechado","resultado":"ganho","valor":1000,"responsavelId":"u2"}]}
                """;

        HttpResponseMessage r = funcao.executar(Fakes.requisicao(corpo), Fakes.contexto());

        assertThat(r.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat((String) r.getBody()).contains("\"valorGanho\":1000").contains("\"Ativo\":1");
    }

    @Test
    void indicadoresResponde400ParaEntradaRuim() {
        var funcao = new IndicadoresFunction(new CalcularIndicadoresHandler());
        assertThat(funcao.executar(Fakes.requisicao("{quebrado"), Fakes.contexto()).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(funcao.executar(Fakes.requisicao(null), Fakes.contexto()).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(funcao.executar(Fakes.requisicao("{\"clientes\":[]}"), Fakes.contexto()).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void oportunidadeGanhaRegistraOResultado() {
        List<String> logs = new ArrayList<>();
        new OportunidadeGanhaFunction(new AtivarClienteHandler((c, e, o) -> ResultadoAtivacao.ATIVADO)).executar(EVENTO, Fakes.contexto(logs));
        assertThat(logs).anyMatch(l -> l.startsWith("INFO") && l.contains("cliente c1: ATIVADO"));
    }

    @Test
    void oportunidadeGanhaAvisaClienteInexistenteSemLancar() {
        List<String> logs = new ArrayList<>();
        new OportunidadeGanhaFunction(new AtivarClienteHandler((c, e, o) -> ResultadoAtivacao.NAO_ENCONTRADO)).executar(EVENTO, Fakes.contexto(logs));
        assertThat(logs).anyMatch(l -> l.startsWith("WARNING"));
    }

    @Test
    void mensagemMalformadaEhDescartada() {
        List<String> logs = new ArrayList<>();
        new OportunidadeGanhaFunction(new AtivarClienteHandler((c, e, o) -> ResultadoAtivacao.ATIVADO)).executar("{nao é json", Fakes.contexto(logs));
        assertThat(logs).anyMatch(l -> l.startsWith("SEVERE") && l.contains("descartada"));
    }

    @Test
    void falhaTemporariaSobeParaRetentativaDoServiceBus() {
        var funcao = new OportunidadeGanhaFunction(new AtivarClienteHandler((c, e, o) -> {
            throw new ErroTransitorioException("503");
        }));
        assertThatThrownBy(() -> funcao.executar(EVENTO, Fakes.contexto())).isInstanceOf(ErroTransitorioException.class);
    }

    @Test
    void documentacaoServeOsArquivosEAsPaginas() {
        var docs = new DocumentacaoFunction();
        assertThat((String) docs.asyncapi(Fakes.requisicao(null), Fakes.contexto()).getBody()).contains("OportunidadeGanha");
        assertThat((String) docs.openapi(Fakes.requisicao(null), Fakes.contexto()).getBody()).contains("/api/indicadores");
        assertThat((String) docs.eventos(Fakes.requisicao(null), Fakes.contexto()).getBody()).contains("AsyncApiStandalone.render");
        assertThat((String) docs.docs(Fakes.requisicao(null), Fakes.contexto()).getBody()).contains("openapi.yaml");
        assertThat(docs.health(Fakes.requisicao(null), Fakes.contexto()).getStatus()).isEqualTo(HttpStatus.OK);
    }
}
