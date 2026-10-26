package br.pjbl.minicrm.clientes.application.registrarinteracao;

import br.pjbl.minicrm.clientes.application.common.Buscar;
import br.pjbl.minicrm.clientes.application.common.ComRetentativa;
import br.pjbl.minicrm.clientes.application.common.InteracaoView;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.Interacao;
import br.pjbl.minicrm.clientes.domain.ValidacaoException;

import java.time.Clock;
import java.time.Instant;

/** RF05: registra uma ligação, e-mail ou reunião com o cliente. */
public class RegistrarInteracaoHandler {

    private final ClienteRepository repositorio;
    private final Clock relogio;

    public RegistrarInteracaoHandler(ClienteRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public InteracaoView executar(RegistrarInteracaoCommand c) {
        Instant agora = relogio.instant();
        Instant data = c.data() == null ? agora : c.data();
        if (data.isAfter(agora.plusSeconds(60))) {
            throw new ValidacaoException("data: não pode estar no futuro");
        }
        Interacao interacao = Interacao.nova(c.tipo(), c.obs(), c.autorId(), data);
        return ComRetentativa.executar(() -> {
            Cliente cliente = Buscar.ouFalhar(repositorio, c.clienteId());
            cliente.registrarInteracao(interacao, agora);
            repositorio.salvar(cliente);
            return InteracaoView.de(interacao);
        });
    }
}
