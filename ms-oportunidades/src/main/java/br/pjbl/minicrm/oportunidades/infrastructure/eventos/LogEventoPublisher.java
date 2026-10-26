package br.pjbl.minicrm.oportunidades.infrastructure.eventos;

import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeGanha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Usado quando SERVICEBUS_CONNECTION não está definida (desenvolvimento e testes): só registra no log. */
public class LogEventoPublisher implements EventoPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(LogEventoPublisher.class);
    private final List<OportunidadeGanha> publicados = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void publicar(OportunidadeGanha evento) {
        publicados.add(evento);
        LOG.info("[sem Service Bus] OportunidadeGanha {} → cliente {}", evento.eventId(), evento.clienteId());
    }

    public List<OportunidadeGanha> publicados() {
        return List.copyOf(publicados);
    }
}
