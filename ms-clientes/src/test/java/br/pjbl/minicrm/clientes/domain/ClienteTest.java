package br.pjbl.minicrm.clientes.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClienteTest {

    private static final Instant AGORA = Instant.parse("2026-11-02T13:00:00Z");
    private static final DadosCliente DADOS = new DadosCliente("Mariana Souza", "Acme Ltda", "Mariana@Acme.com.br", "(41) 99999-0000", "u2");

    @Test
    void nasceComoProspectComEmailNormalizado() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);

        assertThat(cliente.status()).isEqualTo(StatusCliente.PROSPECT);
        assertThat(cliente.email()).isEqualTo("mariana@acme.com.br");
        assertThat(cliente.versao()).isNull();
        assertThat(cliente.excluido()).isFalse();
        assertThat(cliente.criadoEm()).isEqualTo(AGORA);
    }

    @Test
    void devolveTodosOsErrosDeUmaVez() {
        assertThatThrownBy(() -> Cliente.cadastrar(new DadosCliente("Ab", "A", "sem-arroba", "", " "), AGORA))
                .isInstanceOfSatisfying(ValidacaoException.class, e -> assertThat(e.erros()).containsExactly(
                        "nome: deve ter entre 3 e 120 caracteres",
                        "empresa: deve ter entre 2 e 120 caracteres",
                        "email: formato inválido",
                        "responsavelId: obrigatório"));
    }

    @Test
    void alteraSoOsCamposEnviadosETransfereResponsavel() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);
        Instant depois = AGORA.plusSeconds(60);

        cliente.alterar(new AlteracaoCliente(null, "Acme S.A.", null, null, "u5", null), depois);

        assertThat(cliente.nome()).isEqualTo("Mariana Souza");
        assertThat(cliente.empresa()).isEqualTo("Acme S.A.");
        assertThat(cliente.responsavelId()).isEqualTo("u5");
        assertThat(cliente.atualizadoEm()).isEqualTo(depois);
    }

    @Test
    void naoInativaPorAlteracaoNemAceitaAlteracaoVazia() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);

        assertThatThrownBy(() -> cliente.alterar(new AlteracaoCliente(null, null, null, null, null, "Inativo"), AGORA))
                .isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> cliente.alterar(new AlteracaoCliente(null, null, null, null, null, null), AGORA))
                .isInstanceOf(ValidacaoException.class);
        assertThat(cliente.status()).isEqualTo(StatusCliente.PROSPECT);
    }

    @Test
    void exclusaoLogicaViraInativoEBloqueiaAlteracoes() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);

        cliente.excluir(AGORA);
        cliente.excluir(AGORA);

        assertThat(cliente.excluido()).isTrue();
        assertThat(cliente.status()).isEqualTo(StatusCliente.INATIVO);
        assertThatThrownBy(() -> cliente.alterar(new AlteracaoCliente("Outro nome", null, null, null, null, null), AGORA))
                .isInstanceOf(ClienteExcluidoException.class);
        assertThatThrownBy(() -> cliente.registrarInteracao(Interacao.nova("Ligação", "Retornar amanhã", "u2", AGORA), AGORA))
                .isInstanceOf(ClienteExcluidoException.class);
    }

    @Test
    void registraInteracoes() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);

        cliente.registrarInteracao(Interacao.nova("reuniao", "Apresentei a proposta", "u2", AGORA), AGORA);

        assertThat(cliente.interacoes()).singleElement().satisfies(i -> assertThat(i.tipo()).isEqualTo(TipoInteracao.REUNIAO));
    }

    @Test
    void oportunidadeGanhaAtivaOProspectUmaVezSo() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);

        assertThat(cliente.ativarPorOportunidadeGanha("ev-1", AGORA)).isEqualTo(ResultadoAtivacao.ATIVADO);
        assertThat(cliente.ativarPorOportunidadeGanha("ev-1", AGORA)).isEqualTo(ResultadoAtivacao.REPETIDO);
        assertThat(cliente.ativarPorOportunidadeGanha("ev-2", AGORA)).isEqualTo(ResultadoAtivacao.JA_ATIVO);
        assertThat(cliente.status()).isEqualTo(StatusCliente.ATIVO);
    }

    @Test
    void oportunidadeGanhaNaoReativaClienteExcluido() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);
        cliente.excluir(AGORA);

        assertThat(cliente.ativarPorOportunidadeGanha("ev-1", AGORA)).isEqualTo(ResultadoAtivacao.IGNORADO_EXCLUIDO);
        assertThat(cliente.status()).isEqualTo(StatusCliente.INATIVO);
    }

    @Test
    void lembraApenasOsUltimosEventos() {
        Cliente cliente = Cliente.cadastrar(DADOS, AGORA);
        for (int i = 0; i <= Cliente.LIMITE_EVENTOS_LEMBRADOS; i++) {
            cliente.ativarPorOportunidadeGanha("ev-" + i, AGORA);
        }

        assertThat(cliente.estado().eventosProcessados()).hasSize(Cliente.LIMITE_EVENTOS_LEMBRADOS).doesNotContain("ev-0");
    }

    @Test
    void interacaoValidaTipoEObservacao() {
        assertThatThrownBy(() -> Interacao.nova("WhatsApp", "ok", "", AGORA))
                .isInstanceOfSatisfying(ValidacaoException.class, e -> assertThat(e.erros()).hasSize(3));
        assertThat(TipoInteracao.deTexto("e-mail")).contains(TipoInteracao.EMAIL);
        assertThat(StatusCliente.deTexto("ativo")).contains(StatusCliente.ATIVO);
    }
}
