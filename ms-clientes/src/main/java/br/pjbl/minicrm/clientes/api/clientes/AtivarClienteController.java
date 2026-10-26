package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.ativarcliente.AtivacaoView;
import br.pjbl.minicrm.clientes.application.ativarcliente.AtivarClienteCommand;
import br.pjbl.minicrm.clientes.application.ativarcliente.AtivarClienteHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Eventos")
@RestController
@RequestMapping("/clientes")
public class AtivarClienteController {

    private final AtivarClienteHandler handler;

    public AtivarClienteController(AtivarClienteHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/{id}/ativacao")
    @Operation(summary = "Ativa o cliente por oportunidade ganha (RF30)",
            description = "Chamado pela Azure Function ao consumir OportunidadeGanha. Idempotente pelo eventoId: "
                    + "resultado = ATIVADO, JA_ATIVO, REPETIDO ou IGNORADO_EXCLUIDO.")
    public AtivacaoView ativar(@PathVariable String id, @Valid @RequestBody ClienteRequests.Ativacao corpo) {
        return handler.executar(new AtivarClienteCommand(id, corpo.eventoId(), corpo.oportunidadeId()));
    }
}
