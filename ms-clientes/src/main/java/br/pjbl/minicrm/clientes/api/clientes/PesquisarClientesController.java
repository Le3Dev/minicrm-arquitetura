package br.pjbl.minicrm.clientes.api.clientes;

import br.pjbl.minicrm.clientes.application.pesquisarclientes.PesquisarClientesHandler;
import br.pjbl.minicrm.clientes.application.pesquisarclientes.PesquisarClientesQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Clientes")
@RestController
@RequestMapping("/clientes")
public class PesquisarClientesController {

    private final PesquisarClientesHandler handler;

    public PesquisarClientesController(PesquisarClientesHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @Operation(summary = "Pesquisa clientes (RF04, RF18)",
            description = "Busca por nome ou empresa, filtros por status e responsável, 20 por página. Clientes excluídos não aparecem.")
    public PaginaResponse pesquisar(
            @Parameter(description = "Trecho do nome ou da empresa", example = "acme") @RequestParam(required = false) String q,
            @Parameter(description = "Prospect, Ativo ou Inativo", example = "Ativo") @RequestParam(required = false) String status,
            @Parameter(description = "Um ou mais responsáveis (separados por vírgula). O BFF envia o escopo do usuário (RF15).", example = "u2,u5")
            @RequestParam(name = "responsavelId", required = false) List<String> responsaveis,
            @Parameter(description = "Restringe a estes ids (separados por vírgula)") @RequestParam(required = false) List<String> ids,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int porPagina) {
        List<String> resp = Listas.separar(responsaveis);
        var resultado = handler.executar(new PesquisarClientesQuery(q, status, resp, Listas.separar(ids), pagina, porPagina));
        return PaginaResponse.de(resultado, q, status, resp);
    }
}
