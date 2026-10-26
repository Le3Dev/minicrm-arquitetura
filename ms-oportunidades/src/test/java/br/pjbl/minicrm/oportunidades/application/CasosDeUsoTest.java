package br.pjbl.minicrm.oportunidades.application;

import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.criaroportunidade.CriarOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeCommand;
import br.pjbl.minicrm.oportunidades.application.moveroportunidade.MoverOportunidadeHandler;
import br.pjbl.minicrm.oportunidades.domain.MovimentacaoInvalidaException;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeGanha;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeNaoEncontradaException;
import br.pjbl.minicrm.oportunidades.infrastructure.memoria.InMemoryOportunidadeRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CasosDeUsoTest {

    private final Clock relogio = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
    private final InMemoryOportunidadeRepository repositorio = new InMemoryOportunidadeRepository();
    private final List<OportunidadeGanha> publicados = new ArrayList<>();
    private final MoverOportunidadeHandler mover = new MoverOportunidadeHandler(repositorio, publicados::add, relogio);

    private long criar() {
        return new CriarOportunidadeHandler(repositorio, relogio)
                .executar(new CriarOportunidadeCommand("c1", "Plano anual", new BigDecimal("18500"), null, "u2", "Proposta")).id();
    }

    @Test
    void fecharComoGanhoGravaEPublicaOEvento() {
        long id = criar();

        var view = mover.executar(new MoverOportunidadeCommand(id, "Fechado", "ganho", null, false));

        assertThat(view.etapa()).isEqualTo("Fechado");
        assertThat(view.resultado()).isEqualTo("ganho");
        assertThat(publicados).singleElement().satisfies(e -> {
            assertThat(e.oportunidadeId()).isEqualTo(id);
            assertThat(e.clienteId()).isEqualTo("c1");
        });
        assertThat(repositorio.buscarPorId(id).orElseThrow().versao()).isEqualTo(1L);
    }

    @Test
    void perderOuMoverNaoPublica() {
        long id = criar();
        mover.executar(new MoverOportunidadeCommand(id, "Contato", null, null, false));
        mover.executar(new MoverOportunidadeCommand(id, "Fechado", "perdido", "Sem verba", false));
        assertThat(publicados).isEmpty();
    }

    @Test
    void movimentacaoInvalidaNaoGravaNemPublica() {
        long id = criar();
        mover.executar(new MoverOportunidadeCommand(id, "Fechado", "ganho", null, false));

        assertThatThrownBy(() -> mover.executar(new MoverOportunidadeCommand(id, "Proposta", null, null, false)))
                .isInstanceOf(MovimentacaoInvalidaException.class);
        assertThat(publicados).hasSize(1);
        assertThatThrownBy(() -> mover.executar(new MoverOportunidadeCommand(999, "Contato", null, null, false)))
                .isInstanceOf(OportunidadeNaoEncontradaException.class);
    }
}
