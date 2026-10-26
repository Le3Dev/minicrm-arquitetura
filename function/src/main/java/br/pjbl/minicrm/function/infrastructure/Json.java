package br.pjbl.minicrm.function.infrastructure;

import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/** JSON compartilhado: ignora campos extras (o BFF manda objetos completos). */
public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Json() {
    }

    public static <T> T ler(String texto, Class<T> tipo) {
        if (texto == null || texto.isBlank()) {
            throw new EntradaInvalidaException("Corpo vazio");
        }
        try {
            T valor = MAPPER.readValue(texto, tipo);
            if (valor == null) {
                throw new EntradaInvalidaException("Corpo vazio");
            }
            return valor;
        } catch (JsonProcessingException e) {
            throw new EntradaInvalidaException("JSON inválido: " + e.getOriginalMessage());
        }
    }

    public static String escrever(Object valor) {
        try {
            return MAPPER.writeValueAsString(valor);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
