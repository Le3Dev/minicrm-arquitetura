package br.pjbl.minicrm.oportunidades.application.moveroportunidade;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.domain.EventoPublisher;
import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeGanha;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeNaoEncontradaException;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

import java.time.Clock;
import java.util.Optional;

/**
 * RF09: move a oportunidade no funil. Ao fechar como ganho, grava no Azure SQL e
 * depois publica OportunidadeGanha no Service Bus — a Azure Function ativa o cliente (RF30).
 *
 * <p>Ordem: grava primeiro, publica depois. Se a publicação falhar, a exceção sobe (HTTP 503)
 * e a oportunidade fica fechada sem evento; para a versão final, o padrão Outbox resolve isso.
 */
public class MoverOportunidadeHandler {

    private final OportunidadeRepository repositorio;
    private final EventoPublisher eventos;
    private final Clock relogio;

    public MoverOportunidadeHandler(OportunidadeRepository repositorio, EventoPublisher eventos, Clock relogio) {
        this.repositorio = repositorio;
        this.eventos = eventos;
        this.relogio = relogio;
    }

    public OportunidadeView executar(MoverOportunidadeCommand c) {
        Oportunidade oportunidade = repositorio.buscarPorId(c.id()).orElseThrow(() -> new OportunidadeNaoEncontradaException(c.id()));
        Optional<OportunidadeGanha> evento = oportunidade.mover(c.etapa(), c.resultado(), c.motivoPerda(), c.reabrir(), relogio.instant());
        Oportunidade gravada = repositorio.salvar(oportunidade);
        evento.ifPresent(eventos::publicar);
        return OportunidadeView.de(gravada);
    }
}
