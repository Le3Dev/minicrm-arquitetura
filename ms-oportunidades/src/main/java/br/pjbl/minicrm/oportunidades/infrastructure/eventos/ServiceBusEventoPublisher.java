package br.pjbl.minicrm.oportunidades.infrastructure.eventos;

import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeGanha;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

/** Publica OportunidadeGanha na fila oportunidade-ganha (formato do asyncapi.yaml). */
public class ServiceBusEventoPublisher implements EventoPublisher {

    private final ServiceBusSenderClient sender;
    private final ObjectMapper json;

    public ServiceBusEventoPublisher(ServiceBusSenderClient sender, ObjectMapper json) {
        this.sender = sender;
        this.json = json;
    }

    @Override
    public void publicar(OportunidadeGanha evento) {
        ServiceBusMessage mensagem = new ServiceBusMessage(paraJson(evento))
                .setContentType("application/json")
                .setMessageId(evento.eventId())
                .setCorrelationId(evento.eventId())
                .setSubject("OportunidadeGanha");
        sender.sendMessage(mensagem);
    }

    String paraJson(OportunidadeGanha e) {
        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("oportunidadeId", String.valueOf(e.oportunidadeId()));
        dados.put("clienteId", e.clienteId());
        dados.put("valor", e.valor());
        dados.put("responsavelId", e.responsavelId());
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("eventId", e.eventId());
        corpo.put("type", "OportunidadeGanha");
        corpo.put("occurredAt", e.ocorridoEm().toString());
        corpo.put("data", dados);
        try {
            return json.writeValueAsString(corpo);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
