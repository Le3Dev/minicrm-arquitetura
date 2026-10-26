package br.pjbl.minicrm.oportunidades.api.oportunidades;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Oportunidades")
@RestController
@RequestMapping("/oportunidades")
public class MoverOportunidadeController {

    private final MoverOportunidadeHandler handler;

    public MoverOportunidadeController(MoverOportunidadeHandler handler) {
        this.handler = handler;
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Move no funil, fecha ou reabre (RF09, RF29)",
            description = "Fechar como ganho publica o evento OportunidadeGanha na fila oportunidade-ganha.")
    public OportunidadeView mover(@PathVariable long id, @Valid @RequestBody OportunidadeRequests.Movimentacao m) {
        return handler.executar(new MoverOportunidadeCommand(id, m.etapa(), m.resultado(), m.motivoPerda(), Boolean.TRUE.equals(m.reabrir())));
    }
}
