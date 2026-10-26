package br.pjbl.minicrm.function.application.ativarcliente;

import br.pjbl.minicrm.function.domain.OportunidadeGanhaEvento;

/**
 * Caso de uso disparado pelo evento OportunidadeGanha (RF30): pede ao MS Clientes
 * que passe o cliente a Ativo. O eventId vai junto, então uma entrega repetida
 * do Service Bus não tem efeito (idempotência no MS Clientes).
 */
public class AtivarClienteHandler {

    private final ClientesGateway clientes;

    public AtivarClienteHandler(ClientesGateway clientes) {
        this.clientes = clientes;
    }

    public ProcessamentoAtivacao executar(OportunidadeGanhaEvento evento) {
        OportunidadeGanhaEvento valido = evento.validar();
        ResultadoAtivacao resultado = clientes.ativar(valido.data().clienteId(), valido.eventId(), valido.data().oportunidadeId());
        return new ProcessamentoAtivacao(valido.eventId(), valido.data().clienteId(), valido.data().oportunidadeId(), resultado);
    }
}
