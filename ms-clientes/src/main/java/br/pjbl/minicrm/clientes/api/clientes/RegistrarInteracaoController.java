package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.common.InteracaoView;
import br.pjbl.minicrm.clientes.application.registrarinteracao.RegistrarInteracaoCommand;
import br.pjbl.minicrm.clientes.application.registrarinteracao.RegistrarInteracaoHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Interações")
@RestController
@RequestMapping("/clientes")
public class RegistrarInteracaoController {

    private final RegistrarInteracaoHandler handler;

    public RegistrarInteracaoController(RegistrarInteracaoHandler handler) {
        this.handler = handler;
    }

    @PostMapping("/{id}/interacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra uma ligação, e-mail ou reunião (RF05)")
    public InteracaoView registrar(@PathVariable String id, @Valid @RequestBody ClienteRequests.NovaInteracao corpo) {
        return handler.executar(new RegistrarInteracaoCommand(id, corpo.tipo(), corpo.obs(), corpo.autorId(), corpo.data()));
    }
}
