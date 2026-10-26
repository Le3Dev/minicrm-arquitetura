package br.pjbl.minicrm.function.functions;

import br.pjbl.minicrm.function.application.calcularindicadores.CalcularIndicadoresCommand;
import br.pjbl.minicrm.function.application.calcularindicadores.CalcularIndicadoresHandler;
import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import br.pjbl.minicrm.function.domain.Indicadores;
import br.pjbl.minicrm.function.infrastructure.Json;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.util.Map;
import java.util.Optional;

/**
 * HTTP Trigger chamado pelo BFF em GET /aggregated-data:
 * POST /api/indicadores { clientes: [...], oportunidades: [...] } → indicadores do dashboard.
 */
public class IndicadoresFunction {

    private final CalcularIndicadoresHandler handler;

    public IndicadoresFunction() {
        this(new CalcularIndicadoresHandler());
    }

    IndicadoresFunction(CalcularIndicadoresHandler handler) {
        this.handler = handler;
    }

    @FunctionName("indicadores")
    public HttpResponseMessage executar(
            @HttpTrigger(name = "requisicao", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION, route = "indicadores")
            HttpRequestMessage<Optional<String>> requisicao,
            ExecutionContext contexto) {
        try {
            CalcularIndicadoresCommand comando = Json.ler(requisicao.getBody().orElse(null), CalcularIndicadoresCommand.class);
            Indicadores indicadores = handler.executar(comando);
            contexto.getLogger().info("Indicadores calculados: " + indicadores.clientes().total() + " clientes, "
                    + indicadores.oportunidades().total() + " oportunidades");
            return requisicao.createResponseBuilder(HttpStatus.OK)
                    .header("content-type", "application/json")
                    .body(Json.escrever(indicadores))
                    .build();
        } catch (EntradaInvalidaException e) {
            return requisicao.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("content-type", "application/json")
                    .body(Json.escrever(Map.of("codigo", "ENTRADA_INVALIDA", "erro", e.getMessage())))
                    .build();
        }
    }
}
