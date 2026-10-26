package br.pjbl.minicrm.function.infrastructure;

import br.pjbl.minicrm.function.application.ativarcliente.ResultadoAtivacao;
import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import br.pjbl.minicrm.function.domain.ErroTransitorioException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Servidor HTTP de verdade (JDK) imitando o MS Clientes. */
class ClientesHttpGatewayTest {

    private HttpServer servidor;
    private String baseUrl;
    private final AtomicReference<String> ultimaRota = new AtomicReference<>();
    private final AtomicReference<String> ultimoCorpo = new AtomicReference<>();
    private volatile int status;
    private volatile String resposta;
    private volatile long atrasoMs;

    @BeforeEach
    void subir() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", troca -> {
            ultimaRota.set(troca.getRequestMethod() + " " + troca.getRequestURI().getRawPath());
            ultimoCorpo.set(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            try {
                Thread.sleep(atrasoMs);
            } catch (InterruptedException ignorado) {
                Thread.currentThread().interrupt();
            }
            byte[] corpo = resposta.getBytes(StandardCharsets.UTF_8);
            troca.getResponseHeaders().add("content-type", "application/json");
            troca.sendResponseHeaders(status, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        servidor.start();
        baseUrl = "http://127.0.0.1:" + servidor.getAddress().getPort();
    }

    @AfterEach
    void derrubar() {
        servidor.stop(0);
    }

    private void responder(int s, String corpo) {
        status = s;
        resposta = corpo;
    }

    @Test
    void enviaPostParaAtivacaoComEventoId() {
        responder(200, "{\"resultado\":\"ATIVADO\",\"cliente\":{}}");

        ResultadoAtivacao r = new ClientesHttpGateway(baseUrl).ativar("c 1", "ev-1", "123");

        assertThat(r).isEqualTo(ResultadoAtivacao.ATIVADO);
        assertThat(ultimaRota.get()).isEqualTo("POST /clientes/c%201/ativacao");
        assertThat(ultimoCorpo.get()).contains("\"eventoId\":\"ev-1\"").contains("\"oportunidadeId\":\"123\"");
    }

    @Test
    void reconheceRepetidoENaoEncontrado() {
        responder(200, "{\"resultado\":\"REPETIDO\"}");
        assertThat(new ClientesHttpGateway(baseUrl).ativar("c1", "ev", "1")).isEqualTo(ResultadoAtivacao.REPETIDO);

        responder(404, "{\"codigo\":\"NAO_ENCONTRADO\"}");
        assertThat(new ClientesHttpGateway(baseUrl).ativar("c1", "ev", "1")).isEqualTo(ResultadoAtivacao.NAO_ENCONTRADO);
    }

    @Test
    void conflitoE5xxSaoTransitorios400NaoE() {
        responder(409, "{\"codigo\":\"CONCORRENCIA\"}");
        assertThatThrownBy(() -> new ClientesHttpGateway(baseUrl).ativar("c1", "ev", "1")).isInstanceOf(ErroTransitorioException.class);

        responder(503, "{}");
        assertThatThrownBy(() -> new ClientesHttpGateway(baseUrl).ativar("c1", "ev", "1")).isInstanceOf(ErroTransitorioException.class);

        responder(400, "{\"codigo\":\"VALIDACAO\"}");
        assertThatThrownBy(() -> new ClientesHttpGateway(baseUrl).ativar("c1", "ev", "1")).isInstanceOf(EntradaInvalidaException.class);
    }

    @Test
    void redeForaDoArTimeoutEUrlAusenteSaoTransitorios() {
        assertThatThrownBy(() -> new ClientesHttpGateway("http://127.0.0.1:1").ativar("c1", "ev", "1")).isInstanceOf(ErroTransitorioException.class);

        responder(200, "{\"resultado\":\"ATIVADO\"}");
        atrasoMs = 1500;
        assertThatThrownBy(() -> new ClientesHttpGateway(baseUrl, Duration.ofMillis(300)).ativar("c1", "ev", "1"))
                .isInstanceOf(ErroTransitorioException.class);
        atrasoMs = 0;

        assertThatThrownBy(() -> new ClientesHttpGateway(null).ativar("c1", "ev", "1"))
                .isInstanceOf(ErroTransitorioException.class).hasMessageContaining("MS_CLIENTES_URL");
    }

    @Test
    void respostaEstranhaEhTransitoria() {
        responder(200, "{\"resultado\":\"OUTRA_COISA\"}");
        assertThatThrownBy(() -> new ClientesHttpGateway(baseUrl).ativar("c1", "ev", "1")).isInstanceOf(ErroTransitorioException.class);
    }

    @Test
    void configuracaoRemoveBarraFinal() {
        assertThat(Configuracao.de(java.util.Map.of("MS_CLIENTES_URL", "https://x.io/ ")).msClientesUrl()).isEqualTo("https://x.io");
        assertThat(Configuracao.de(java.util.Map.of()).msClientesUrl()).isNull();
    }
}
