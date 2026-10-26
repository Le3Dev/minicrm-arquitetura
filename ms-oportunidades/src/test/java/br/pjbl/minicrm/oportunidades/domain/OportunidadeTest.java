package br.pjbl.minicrm.oportunidades.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Regras do funil (seção 5.2 da SPEC do Mini CRM). */
class OportunidadeTest {

    private static final Instant AGORA = Instant.parse("2026-11-10T12:00:00Z");

    private static Oportunidade gravada(Etapa etapa) {
        Oportunidade o = Oportunidade.criar("c1", "Plano anual", new BigDecimal("18500"), LocalDate.of(2026, 12, 15), "u2", null, AGORA);
        Oportunidade.Estado e = o.estado();
        return Oportunidade.restaurar(new Oportunidade.Estado(7L, e.clienteId(), e.titulo(), etapa, null, null, e.valor(), e.dataPrevista(),
                e.responsavelId(), e.criadaEm(), e.atualizadaEm(), 0L));
    }

    @Test
    void nasceEmProspeccaoEValidaTodosOsCampos() {
        assertThat(gravada(Etapa.PROSPECCAO).etapa()).isEqualTo(Etapa.PROSPECCAO);
        assertThatThrownBy(() -> Oportunidade.criar(" ", "AB", BigDecimal.ZERO, null, "", "Fechado", AGORA))
                .isInstanceOfSatisfying(ValidacaoException.class, e -> assertThat(e.erros()).hasSize(5));
    }

    @Test
    void moveLivreEntreEtapasAbertasInclusiveParaTras() {
        Oportunidade o = gravada(Etapa.PROPOSTA);
        assertThat(o.mover("Contato", null, null, false, AGORA)).isEmpty();
        assertThat(o.etapa()).isEqualTo(Etapa.CONTATO);
    }

    @Test
    void fecharComoGanhoGeraOEvento() {
        Oportunidade o = gravada(Etapa.PROPOSTA);

        var evento = o.mover("Fechado", "ganho", null, false, AGORA).orElseThrow();

        assertThat(evento.oportunidadeId()).isEqualTo(7L);
        assertThat(evento.clienteId()).isEqualTo("c1");
        assertThat(evento.valor()).isEqualByComparingTo("18500");
        assertThat(evento.eventId()).isNotBlank();
        assertThat(o.resultado()).isEqualTo(Resultado.GANHO);
    }

    @Test
    void fecharExigeResultadoEPerdidoExigeMotivo() {
        assertThatThrownBy(() -> gravada(Etapa.PROPOSTA).mover("Fechado", null, null, false, AGORA)).isInstanceOf(ValidacaoException.class);
        assertThatThrownBy(() -> gravada(Etapa.PROPOSTA).mover("Fechado", "perdido", "Outro", false, AGORA)).isInstanceOf(ValidacaoException.class);

        Oportunidade o = gravada(Etapa.PROPOSTA);
        assertThat(o.mover("Fechado", "perdido", "preco", false, AGORA)).isEmpty();
        assertThat(o.motivoPerda()).isEqualTo(MotivoPerda.PRECO);
    }

    @Test
    void sairDoFechadoExigeReabrirELimpaOResultado() {
        Oportunidade o = gravada(Etapa.PROPOSTA);
        o.mover("Fechado", "perdido", "Concorrente", false, AGORA);

        assertThatThrownBy(() -> o.mover("Proposta", null, null, false, AGORA)).isInstanceOf(MovimentacaoInvalidaException.class);
        assertThatThrownBy(() -> o.mover("Fechado", "ganho", null, true, AGORA)).isInstanceOf(MovimentacaoInvalidaException.class);

        o.mover("Proposta", null, null, true, AGORA);
        assertThat(o.etapa()).isEqualTo(Etapa.PROPOSTA);
        assertThat(o.resultado()).isNull();
        assertThat(o.motivoPerda()).isNull();
    }
}
