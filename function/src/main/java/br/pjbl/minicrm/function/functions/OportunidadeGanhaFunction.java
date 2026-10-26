package br.pjbl.minicrm.function.functions;

import br.pjbl.minicrm.function.application.ativarcliente.AtivarClienteHandler;
import br.pjbl.minicrm.function.application.ativarcliente.ProcessamentoAtivacao;
import br.pjbl.minicrm.function.domain.EntradaInvalidaException;
import br.pjbl.minicrm.function.domain.OportunidadeGanhaEvento;
import br.pjbl.minicrm.function.infrastructure.ClientesHttpGateway;
import br.pjbl.minicrm.function.infrastructure.Configuracao;
import br.pjbl.minicrm.function.infrastructure.Json;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.ServiceBusQueueTrigger;

/**
 * Service Bus Trigger: consome OportunidadeGanha e pede ao MS Clientes que ative o cliente (RF30).
 * Se lançar exceção, o Service Bus entrega a mensagem de novo (até o maxDeliveryCount da fila)
 * e depois a move para a dead-letter queue.
 */
public class OportunidadeGanhaFunction {

    public static final String FILA = "oportunidade-ganha";

    private final AtivarClienteHandler handler;

    public OportunidadeGanhaFunction() {
        this(new AtivarClienteHandler(new ClientesHttpGateway(Configuracao.doAmbiente().msClientesUrl())));
    }

    OportunidadeGanhaFunction(AtivarClienteHandler handler) {
        this.handler = handler;
    }

    @FunctionName("oportunidadeGanha")
    public void executar(
            @ServiceBusQueueTrigger(name = "mensagem", queueName = FILA, connection = "ServiceBusConnection") String mensagem,
            ExecutionContext contexto) {
        ProcessamentoAtivacao resultado;
        try {
            resultado = handler.executar(Json.ler(mensagem, OportunidadeGanhaEvento.class));
        } catch (EntradaInvalidaException e) {
            // Repetir não muda nada: registra e conclui a mensagem.
            contexto.getLogger().severe("Mensagem descartada: " + e.getMessage());
            return;
        }
        String resumo = "OportunidadeGanha " + resultado.eventId() + " (oportunidade " + resultado.oportunidadeId()
                + ") → cliente " + resultado.clienteId() + ": " + resultado.resultado();
        if (resultado.pendente()) {
            contexto.getLogger().warning(resumo + " — cliente inexistente, verificar");
        } else {
            contexto.getLogger().info(resumo);
        }
    }
}
