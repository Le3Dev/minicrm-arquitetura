package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.buscarcliente.BuscarClienteHandler;
import br.pjbl.minicrm.clientes.application.buscarcliente.BuscarClienteQuery;
import br.pjbl.minicrm.clientes.application.buscarcliente.ClienteDetalheView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/clientes")
public class BuscarClienteController {

    private final BuscarClienteHandler handler;

    public BuscarClienteController(BuscarClienteHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe do cliente com o histórico de interações (RF06)",
            description = "Cliente excluído continua consultável, com excluido=true e status Inativo.")
    public ClienteDetalheView buscar(@PathVariable String id) {
        return handler.executar(new BuscarClienteQuery(id));
    }
}
