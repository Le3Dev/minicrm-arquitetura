package br.pjbl.minicrm.function.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Documentação pública servida pela própria Function:
 * /api/docs (Swagger UI da rota HTTP), /api/eventos (AsyncAPI do OportunidadeGanha), /api/health.
 */
public class DocumentacaoFunction {

    static final String PAGINA_SWAGGER = """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Mini CRM — Function (Swagger)</title>
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
            </head>
            <body>
              <div id="swagger-ui"></div>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
              <script>SwaggerUIBundle({ url: 'openapi.yaml', dom_id: '#swagger-ui' });</script>
            </body>
            </html>
            """;

    static final String PAGINA_EVENTOS = """
            <!doctype html>
            <html lang="pt-BR">
            <head>
              <meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">
              <title>Mini CRM — Eventos (AsyncAPI)</title>
              <link rel="stylesheet" href="https://unpkg.com/@asyncapi/react-component@3/styles/default.min.css">
            </head>
            <body>
              <div id="asyncapi"></div>
              <script src="https://unpkg.com/@asyncapi/react-component@3/browser/standalone/index.js"></script>
              <script>
                AsyncApiStandalone.render(
                  { schema: { url: 'asyncapi.yaml', options: { method: 'GET', mode: 'cors' } }, config: { show: { sidebar: true } } },
                  document.getElementById('asyncapi'));
              </script>
            </body>
            </html>
            """;

    @FunctionName("docs")
    public HttpResponseMessage docs(
            @HttpTrigger(name = "r", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "docs")
            HttpRequestMessage<Optional<String>> r, ExecutionContext c) {
        return responder(r, "text/html; charset=utf-8", PAGINA_SWAGGER);
    }

    @FunctionName("openapi")
    public HttpResponseMessage openapi(
            @HttpTrigger(name = "r", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "openapi.yaml")
            HttpRequestMessage<Optional<String>> r, ExecutionContext c) {
        return responder(r, "application/yaml; charset=utf-8", recurso("docs/openapi.yaml"));
    }

    @FunctionName("eventos")
    public HttpResponseMessage eventos(
            @HttpTrigger(name = "r", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "eventos")
            HttpRequestMessage<Optional<String>> r, ExecutionContext c) {
        return responder(r, "text/html; charset=utf-8", PAGINA_EVENTOS);
    }

    @FunctionName("asyncapi")
    public HttpResponseMessage asyncapi(
            @HttpTrigger(name = "r", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "asyncapi.yaml")
            HttpRequestMessage<Optional<String>> r, ExecutionContext c) {
        return responder(r, "application/yaml; charset=utf-8", recurso("docs/asyncapi.yaml"));
    }

    @FunctionName("health")
    public HttpResponseMessage health(
            @HttpTrigger(name = "r", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS, route = "health")
            HttpRequestMessage<Optional<String>> r, ExecutionContext c) {
        return responder(r, "application/json", "{\"status\":\"ok\",\"servico\":\"minicrm-function\"}");
    }

    static String recurso(String caminho) {
        try (InputStream in = DocumentacaoFunction.class.getClassLoader().getResourceAsStream(caminho)) {
            if (in == null) {
                throw new IllegalStateException("Recurso não encontrado: " + caminho);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static HttpResponseMessage responder(HttpRequestMessage<?> r, String tipo, String corpo) {
        return r.createResponseBuilder(HttpStatus.OK)
                .header("content-type", tipo)
                .header("access-control-allow-origin", "*")
                .body(corpo)
                .build();
    }
}
