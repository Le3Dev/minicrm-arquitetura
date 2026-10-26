package br.pjbl.minicrm.clientes.application.ativarcliente;

import br.pjbl.minicrm.clientes.application.common.Buscar;
import br.pjbl.minicrm.clientes.application.common.ClienteView;
import br.pjbl.minicrm.clientes.application.common.ComRetentativa;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.ResultadoAtivacao;

import java.time.Clock;

/**
 * RF30 dirigido por evento: a Azure Function consome OportunidadeGanha e chama este caso de uso.
 * Idempotente: a mesma mensagem entregue duas vezes não muda nada na segunda.
 */
public class AtivarClienteHandler {

    private final ClienteRepository repositorio;
    private final Clock relogio;

    public AtivarClienteHandler(ClienteRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public AtivacaoView executar(AtivarClienteCommand c) {
        return ComRetentativa.executar(() -> {
            Cliente cliente = Buscar.ouFalhar(repositorio, c.clienteId());
            ResultadoAtivacao resultado = cliente.ativarPorOportunidadeGanha(c.eventoId(), relogio.instant());
            if (resultado == ResultadoAtivacao.ATIVADO || resultado == ResultadoAtivacao.JA_ATIVO) {
                repositorio.salvar(cliente);
            }
            return new AtivacaoView(resultado.name(), ClienteView.de(cliente));
        });
    }
}
