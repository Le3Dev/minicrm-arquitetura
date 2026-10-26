package br.pjbl.minicrm.clientes.application;

import br.pjbl.minicrm.clientes.application.ativarcliente.AtivarClienteCommand;
import br.pjbl.minicrm.clientes.application.ativarcliente.AtivarClienteHandler;
import br.pjbl.minicrm.clientes.application.atualizarcliente.AtualizarClienteCommand;
import br.pjbl.minicrm.clientes.application.atualizarcliente.AtualizarClienteHandler;
import br.pjbl.minicrm.clientes.application.buscarcliente.BuscarClienteHandler;
import br.pjbl.minicrm.clientes.application.buscarcliente.BuscarClienteQuery;
import br.pjbl.minicrm.clientes.application.cadastrarcliente.CadastrarClienteCommand;
import br.pjbl.minicrm.clientes.application.cadastrarcliente.CadastrarClienteHandler;
import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.application.excluircliente.ExcluirClienteCommand;
import br.pjbl.minicrm.clientes.application.excluircliente.ExcluirClienteHandler;
import br.pjbl.minicrm.clientes.application.pesquisarclientes.PesquisarClientesHandler;
import br.pjbl.minicrm.clientes.application.pesquisarclientes.PesquisarClientesQuery;
import br.pjbl.minicrm.clientes.application.registrarinteracao.RegistrarInteracaoCommand;
import br.pjbl.minicrm.clientes.application.registrarinteracao.RegistrarInteracaoHandler;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteNaoEncontradoException;
import br.pjbl.minicrm.clientes.domain.ConcorrenciaException;
import br.pjbl.minicrm.clientes.domain.ValidacaoException;
import br.pjbl.minicrm.clientes.infrastructure.memoria.InMemoryClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Cada caso de uso (vertical slice) exercitado com o repositório em memória. */
class CasosDeUsoTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-11-02T13:00:00Z"), ZoneOffset.UTC);
    private InMemoryClienteRepository repositorio;
    private CadastrarClienteHandler cadastrar;

    @BeforeEach
    void preparar() {
        repositorio = new InMemoryClienteRepository();
        cadastrar = new CadastrarClienteHandler(repositorio, relogio);
    }

    private ClienteView novo(String nome, String empresa, String responsavel) {
        return cadastrar.executar(new CadastrarClienteCommand(nome, empresa, nome.toLowerCase().replace(' ', '.') + "@x.com", "", responsavel));
    }

    @Test
    void cadastrarGravaEDevolveProspect() {
        ClienteView view = novo("Mariana Souza", "Acme", "u2");

        assertThat(view.status()).isEqualTo("Prospect");
        assertThat(repositorio.buscarPorId(view.id())).isPresent();
    }

    @Test
    void buscarDevolveInteracoesDaMaisRecenteParaAMaisAntiga() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");
        var registrar = new RegistrarInteracaoHandler(repositorio, relogio);
        registrar.executar(new RegistrarInteracaoCommand(c.id(), "Ligação", "Primeiro contato", "u2", Instant.parse("2026-10-01T10:00:00Z")));
        registrar.executar(new RegistrarInteracaoCommand(c.id(), "Reunião", "Apresentação", "u2", null));

        var detalhe = new BuscarClienteHandler(repositorio).executar(new BuscarClienteQuery(c.id()));

        assertThat(detalhe.interacoes()).extracting(i -> i.tipo()).containsExactly("Reunião", "Ligação");
        assertThat(detalhe.cliente().totalInteracoes()).isEqualTo(2);
        assertThat(detalhe.cliente().ultimaInteracaoEm()).isEqualTo(relogio.instant());
    }

    @Test
    void buscarIdInexistenteLancaNaoEncontrado() {
        assertThatThrownBy(() -> new BuscarClienteHandler(repositorio).executar(new BuscarClienteQuery("x")))
                .isInstanceOf(ClienteNaoEncontradoException.class);
    }

    @Test
    void registrarInteracaoRecusaDataFutura() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");

        assertThatThrownBy(() -> new RegistrarInteracaoHandler(repositorio, relogio)
                .executar(new RegistrarInteracaoCommand(c.id(), "Ligação", "Retornar", "u2", relogio.instant().plusSeconds(3600))))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void pesquisarFiltraPorTextoSemAcentoStatusResponsavelEPagina() {
        novo("João Pereira", "Café Brasil", "u2");
        novo("Ana Lima", "Acme", "u5");
        ClienteView bruno = novo("Bruno Alves", "Acme", "u2");
        new AtivarClienteHandler(repositorio, relogio).executar(new AtivarClienteCommand(bruno.id(), "ev-1", "1"));
        var pesquisar = new PesquisarClientesHandler(repositorio);

        assertThat(pesquisar.executar(new PesquisarClientesQuery("cafe", null, null, null, null, null)).itens())
                .extracting(ClienteView::nome).containsExactly("João Pereira");
        assertThat(pesquisar.executar(new PesquisarClientesQuery("ACME", "ativo", null, null, null, null)).itens())
                .extracting(ClienteView::nome).containsExactly("Bruno Alves");
        assertThat(pesquisar.executar(new PesquisarClientesQuery(null, null, List.of("u2"), null, null, null)).total()).isEqualTo(2);

        var pagina2 = pesquisar.executar(new PesquisarClientesQuery(null, null, null, null, 2, 2));
        assertThat(pagina2.itens()).extracting(ClienteView::nome).containsExactly("João Pereira");
        assertThat(pagina2.totalPaginas()).isEqualTo(2);
    }

    @Test
    void pesquisarRecusaStatusInvalido() {
        assertThatThrownBy(() -> new PesquisarClientesHandler(repositorio).executar(new PesquisarClientesQuery(null, "Lead", null, null, null, null)))
                .isInstanceOf(ValidacaoException.class);
    }

    @Test
    void excluidoSomeDaPesquisaMasContinuaConsultavel() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");

        new ExcluirClienteHandler(repositorio, relogio).executar(new ExcluirClienteCommand(c.id()));
        new ExcluirClienteHandler(repositorio, relogio).executar(new ExcluirClienteCommand(c.id()));

        assertThat(new PesquisarClientesHandler(repositorio).executar(new PesquisarClientesQuery(null, null, null, null, null, null)).total()).isZero();
        assertThat(new BuscarClienteHandler(repositorio).executar(new BuscarClienteQuery(c.id())).cliente().status()).isEqualTo("Inativo");
    }

    @Test
    void atualizarAplicaPatch() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");

        ClienteView alterado = new AtualizarClienteHandler(repositorio, relogio)
                .executar(new AtualizarClienteCommand(c.id(), null, null, "nova@acme.com", null, "u5", null));

        assertThat(alterado.email()).isEqualTo("nova@acme.com");
        assertThat(alterado.responsavelId()).isEqualTo("u5");
        assertThat(alterado.nome()).isEqualTo("Mariana Souza");
    }

    @Test
    void ativarEhIdempotente() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");
        var ativar = new AtivarClienteHandler(repositorio, relogio);

        assertThat(ativar.executar(new AtivarClienteCommand(c.id(), "ev-9", "9")).resultado()).isEqualTo("ATIVADO");
        assertThat(ativar.executar(new AtivarClienteCommand(c.id(), "ev-9", "9")).resultado()).isEqualTo("REPETIDO");
        assertThat(repositorio.buscarPorId(c.id()).orElseThrow().versao()).isEqualTo(1L);
    }

    @Test
    void tentaDeNovoQuandoOutraGravacaoVenceuACorrida() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");
        AtomicInteger chamadas = new AtomicInteger();
        var repositorioDisputado = new InMemoryClienteRepository() {
            @Override
            public synchronized void salvar(Cliente cliente) {
                if (chamadas.incrementAndGet() == 1) {
                    throw new ConcorrenciaException(cliente.id());
                }
                repositorio.salvar(cliente);
            }

            @Override
            public java.util.Optional<Cliente> buscarPorId(String id) {
                return repositorio.buscarPorId(id);
            }
        };

        var resultado = new AtivarClienteHandler(repositorioDisputado, relogio).executar(new AtivarClienteCommand(c.id(), "ev-1", "1"));

        assertThat(chamadas.get()).isEqualTo(2);
        assertThat(resultado.resultado()).isEqualTo("ATIVADO");
    }

    @Test
    void repositorioRecusaGravacaoComVersaoDesatualizada() {
        ClienteView c = novo("Mariana Souza", "Acme", "u2");
        Cliente primeiro = repositorio.buscarPorId(c.id()).orElseThrow();
        Cliente segundo = repositorio.buscarPorId(c.id()).orElseThrow();
        primeiro.excluir(relogio.instant());
        repositorio.salvar(primeiro);
        segundo.ativarPorOportunidadeGanha("ev-1", relogio.instant());

        assertThatThrownBy(() -> repositorio.salvar(segundo)).isInstanceOf(ConcorrenciaException.class);
    }
}
