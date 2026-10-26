package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.cadastrarcliente.CadastrarClienteCommand;
import br.pjbl.minicrm.clientes.application.cadastrarcliente.CadastrarClienteHandler;
import br.pjbl.minicrm.clientes.application.common.ClienteView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/clientes")
public class CadastrarClienteController {

    private final CadastrarClienteHandler handler;

    public CadastrarClienteController(CadastrarClienteHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    @Operation(summary = "Cadastra um cliente (RF03). Ele nasce como Prospect.")
    public ResponseEntity<ClienteView> cadastrar(@Valid @RequestBody ClienteRequests.NovoCliente corpo) {
        ClienteView criado = handler.executar(new CadastrarClienteCommand(corpo.nome(), corpo.empresa(), corpo.email(), corpo.telefone(), corpo.responsavelId()));
        return ResponseEntity.created(URI.create("/clientes/" + criado.id())).body(criado);
    }
}
