package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.atualizarcliente.AtualizarClienteCommand;
import br.pjbl.minicrm.clientes.application.atualizarcliente.AtualizarClienteHandler;
import br.pjbl.minicrm.clientes.application.common.ClienteView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/clientes")
public class AtualizarClienteController {

    private final AtualizarClienteHandler handler;

    public AtualizarClienteController(AtualizarClienteHandler handler) {
        this.handler = handler;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Altera dados do cliente ou transfere o responsável (RF03, RF26)")
    public ClienteView atualizar(@PathVariable String id, @Valid @RequestBody ClienteRequests.Alteracao corpo) {
        return handler.executar(new AtualizarClienteCommand(id, corpo.nome(), corpo.empresa(), corpo.email(), corpo.telefone(),
                corpo.responsavelId(), corpo.status()));
    }
}
