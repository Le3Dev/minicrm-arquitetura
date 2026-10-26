package br.pjbl.minicrm.clientes.config;

import br.pjbl.minicrm.clientes.application.ativarcliente.AtivarClienteHandler;
import br.pjbl.minicrm.clientes.application.atualizarcliente.AtualizarClienteHandler;
import br.pjbl.minicrm.clientes.application.buscarcliente.BuscarClienteHandler;
import br.pjbl.minicrm.clientes.application.cadastrarcliente.CadastrarClienteHandler;
import br.pjbl.minicrm.clientes.application.excluircliente.ExcluirClienteHandler;
import br.pjbl.minicrm.clientes.application.pesquisarclientes.PesquisarClientesHandler;
import br.pjbl.minicrm.clientes.application.registrarinteracao.RegistrarInteracaoHandler;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Raiz de composição: os casos de uso são classes Java puras (sem anotação do Spring);
 * aqui eles recebem o repositório e o relógio.
 */
@Configuration
public class CasosDeUsoConfig {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }

    @Bean
    CadastrarClienteHandler cadastrarCliente(ClienteRepository r, Clock c) { return new CadastrarClienteHandler(r, c); }

    @Bean
    BuscarClienteHandler buscarCliente(ClienteRepository r) { return new BuscarClienteHandler(r); }

    @Bean
    PesquisarClientesHandler pesquisarClientes(ClienteRepository r) { return new PesquisarClientesHandler(r); }

    @Bean
    AtualizarClienteHandler atualizarCliente(ClienteRepository r, Clock c) { return new AtualizarClienteHandler(r, c); }

    @Bean
    ExcluirClienteHandler excluirCliente(ClienteRepository r, Clock c) { return new ExcluirClienteHandler(r, c); }

    @Bean
    RegistrarInteracaoHandler registrarInteracao(ClienteRepository r, Clock c) { return new RegistrarInteracaoHandler(r, c); }

    @Bean
    AtivarClienteHandler ativarCliente(ClienteRepository r, Clock c) { return new AtivarClienteHandler(r, c); }
}
