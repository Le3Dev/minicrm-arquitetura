package br.pjbl.minicrm.oportunidades.config;

import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Raiz de composição dos casos de uso (classes Java puras). TODO (aluno B): registrar as novas slices aqui. */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    CriarOportunidadeHandler criarOportunidade(OportunidadeRepository r, Clock c) {
        return new CriarOportunidadeHandler(r, c);
    }

    @Bean
    MoverOportunidadeHandler moverOportunidade(OportunidadeRepository r, EventoPublisher e, Clock c) {
        return new MoverOportunidadeHandler(r, e, c);
    }
}
