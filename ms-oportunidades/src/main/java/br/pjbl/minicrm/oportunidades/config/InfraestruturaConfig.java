package br.pjbl.minicrm.oportunidades.config;

import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;
import br.pjbl.minicrm.oportunidades.infrastructure.eventos.LogEventoPublisher;
import br.pjbl.minicrm.oportunidades.infrastructure.eventos.ServiceBusEventoPublisher;
import br.pjbl.minicrm.oportunidades.infrastructure.jpa.JpaOportunidadeRepository;
import br.pjbl.minicrm.oportunidades.infrastructure.jpa.OportunidadeJpaRepository;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Liga as portas aos adaptadores: Azure SQL (JPA) e Azure Service Bus. */
@Configuration
public class InfraestruturaConfig {

    @Bean
    OportunidadeRepository oportunidadeRepository(OportunidadeJpaRepository jpa) {
        return new JpaOportunidadeRepository(jpa);
    }

    /** Sem SERVICEBUS_CONNECTION, os eventos só vão para o log (desenvolvimento e testes). */
    @Bean
    EventoPublisher eventoPublisher(@Value("${app.servicebus.connection:}") String conexao,
                                    @Value("${app.servicebus.fila:oportunidade-ganha}") String fila,
                                    ObjectMapper json) {
        if (conexao.isBlank()) {
            return new LogEventoPublisher();
        }
        return new ServiceBusEventoPublisher(new ServiceBusClientBuilder().connectionString(conexao).sender().queueName(fila).buildClient(), json);
    }
}
