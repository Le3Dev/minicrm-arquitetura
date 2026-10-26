package br.pjbl.minicrm.function.infrastructure;

import br.pjbl.minicrm.function.application.ativarcliente.ClientesGateway;
import br.pjbl.minicrm.function.application.ativarcliente.ResultadoAtivacao;
import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import br.pjbl.minicrm.function.domain.ErroTransitorioException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/** Adaptador HTTP da porta ClientesGateway: POST /clientes/{id}/ativacao no MS Clientes. */
public class ClientesHttpGateway implements ClientesGateway {

    private final String baseUrl;
    private final HttpClient http;
    private final Duration timeout;

    public ClientesHttpGateway(String baseUrl, Duration timeout) {
        this.baseUrl = baseUrl;
        this.timeout = timeout;
        this.http = HttpClient.newBuilder().connectTimeout(timeout).build();
    }

    public ClientesHttpGateway(String baseUrl) {
        this(baseUrl, Duration.ofSeconds(10));
    }

    @Override
    public ResultadoAtivacao ativar(String clienteId, String eventoId, String oportunidadeId) {
        if (baseUrl == null) {
            throw new ErroTransitorioException("A configuração MS_CLIENTES_URL não foi definida");
        }
        URI uri = URI.create(baseUrl + "/clientes/" + URLEncoder.encode(clienteId, StandardCharsets.UTF_8).replace("+", "%20") + "/ativacao");
        String corpo = Json.escrever(Map.of("eventoId", eventoId, "oportunidadeId", oportunidadeId));
        HttpRequest requisicao = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo))
                .build();

        HttpResponse<String> resposta;
        try {
            resposta = http.send(requisicao, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new ErroTransitorioException("Falha de rede ao chamar o MS Clientes (" + uri + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ErroTransitorioException("Chamada ao MS Clientes interrompida", e);
        }

        int status = resposta.statusCode();
        if (status == 200) {
            String resultado = lerCampo(resposta.body(), "resultado");
            try {
                return ResultadoAtivacao.valueOf(resultado);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new ErroTransitorioException("Resposta inesperada do MS Clientes: " + resposta.body());
            }
        }
        if (status == 404) {
            return ResultadoAtivacao.NAO_ENCONTRADO;
        }
        if (status == 400) {
            throw new EntradaInvalidaException("O MS Clientes recusou a ativação: " + resposta.body());
        }
        // 409 CONCORRENCIA, 5xx e demais: tentar de novo
        throw new ErroTransitorioException("O MS Clientes respondeu " + status + ": " + resposta.body());
    }

    private static String lerCampo(String corpo, String campo) {
        try {
            JsonNode no = Json.MAPPER.readTree(corpo);
            return no.path(campo).isTextual() ? no.get(campo).asText() : null;
        } catch (IOException e) {
            return null;
        }
    }
}
