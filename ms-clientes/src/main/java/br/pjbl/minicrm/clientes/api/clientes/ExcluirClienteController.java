package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.excluircliente.ExcluirClienteCommand;
import br.pjbl.minicrm.clientes.application.excluircliente.ExcluirClienteHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/clientes")
public class ExcluirClienteController {

    private final ExcluirClienteHandler handler;

    public ExcluirClienteController(ExcluirClienteHandler handler) {
        this.handler = handler;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclusão lógica (RF03): o cliente vira Inativo e some das listagens")
    public void excluir(@PathVariable String id) {
        handler.executar(new ExcluirClienteCommand(id));
    }
}
