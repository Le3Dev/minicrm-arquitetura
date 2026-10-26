package br.pjbl.minicrm.oportunidades.api.oportunidades;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Oportunidades")
@RestController
@RequestMapping("/oportunidades")
public class CriarOportunidadeController {

    private final CriarOportunidadeHandler handler;

    public CriarOportunidadeController(CriarOportunidadeHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    @Operation(summary = "Cria oportunidade vinculada a um cliente (RF07)")
    public ResponseEntity<OportunidadeView> criar(@Valid @RequestBody OportunidadeRequests.Nova c) {
        OportunidadeView v = handler.executar(new CriarOportunidadeCommand(c.clienteId(), c.titulo(), c.valor(), c.dataPrevista(), c.responsavelId(), c.etapa()));
        return ResponseEntity.created(URI.create("/oportunidades/" + v.id())).body(v);
    }
}
