package br.pjbl.minicrm.clientes.application.excluircliente;

import br.pjbl.minicrm.clientes.application.common.Buscar;
import br.pjbl.minicrm.clientes.application.common.ComRetentativa;
import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;

import java.time.Clock;

/** RF03: exclusão lógica — o cliente vira Inativo, some das listagens e o histórico é preservado. */
public class ExcluirClienteHandler {

    private final ClienteRepository repositorio;
    private final Clock relogio;

    public ExcluirClienteHandler(ClienteRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public void executar(ExcluirClienteCommand c) {
        ComRetentativa.executar(() -> {
            Cliente cliente = Buscar.ouFalhar(repositorio, c.id());
            if (!cliente.excluido()) {
                cliente.excluir(relogio.instant());
                repositorio.salvar(cliente);
            }
            return null;
        });
    }
}
