package br.pjbl.minicrm.function.functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Substitutos do runtime do Azure Functions para os testes dos gatilhos. */
final class Fakes {

    private Fakes() {
    }

    record Resposta(HttpStatusType status, Map<String, String> headers, Object corpo) implements HttpResponseMessage {
        @Override
        public HttpStatusType getStatus() { return status; }
        @Override
        public String getHeader(String nome) { return headers.get(nome); }
        @Override
        public Object getBody() { return corpo; }
    }

    static final class Builder implements HttpResponseMessage.Builder {
        private HttpStatusType status;
        private final Map<String, String> headers = new HashMap<>();
        private Object corpo;

        Builder(HttpStatusType status) { this.status = status; }
        @Override
        public HttpResponseMessage.Builder status(HttpStatusType s) { this.status = s; return this; }
        @Override
        public HttpResponseMessage.Builder header(String k, String v) { headers.put(k, v); return this; }
        @Override
        public HttpResponseMessage.Builder body(Object b) { this.corpo = b; return this; }
        @Override
        public HttpResponseMessage build() { return new Resposta(status, headers, corpo); }
    }

    @SuppressWarnings("unchecked")
    static HttpRequestMessage<Optional<String>> requisicao(String corpo) {
        HttpRequestMessage<Optional<String>> r = mock(HttpRequestMessage.class);
        when(r.getBody()).thenReturn(Optional.ofNullable(corpo));
        when(r.createResponseBuilder(any(HttpStatus.class))).thenAnswer(i -> new Builder(i.getArgument(0)));
        return r;
    }

    /** Contexto cujo logger guarda as mensagens em {@code logs} como "NIVEL: texto". */
    static ExecutionContext contexto(List<String> logs) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.ALL);
        logger.addHandler(new Handler() {
            @Override
            public void publish(LogRecord r) { logs.add(r.getLevel().getName() + ": " + r.getMessage()); }
            @Override
            public void flush() { }
            @Override
            public void close() { }
        });
        ExecutionContext c = mock(ExecutionContext.class);
        when(c.getLogger()).thenReturn(logger);
        return c;
    }

    static ExecutionContext contexto() {
        return contexto(new ArrayList<>());
    }
}
