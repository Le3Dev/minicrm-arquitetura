package br.pjbl.minicrm.clientes.config;

import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.infrastructure.memoria.InMemoryClienteRepository;
import br.pjbl.minicrm.clientes.infrastructure.mongo.MongoClienteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;

/** Liga a porta ClienteRepository ao adaptador: MongoDB (padrão) ou memória (perfil "memoria"). */
@Configuration
public class PersistenciaConfig {

    @Bean
    @Profile("!memoria")
    ClienteRepository clienteRepositoryMongo(MongoTemplate mongo) {
        return new MongoClienteRepository(mongo);
    }

    @Bean
    @Profile("memoria")
    ClienteRepository clienteRepositoryMemoria() {
        return new InMemoryClienteRepository();
    }
}
