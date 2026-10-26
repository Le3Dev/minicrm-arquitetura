package br.pjbl.minicrm.oportunidades.application.criaroportunidade;

import br.pjbl.minicrm.oportunidades.application.common.OportunidadeView;
import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;

import java.time.Clock;

/**
 * RF07: cria a oportunidade vinculada a um cliente.
 * A existência do cliente é conferida pelo BFF antes de chamar este serviço (os MS não se chamam).
 */
public class CriarOportunidadeHandler {

    private final OportunidadeRepository repositorio;
    private final Clock relogio;

    public CriarOportunidadeHandler(OportunidadeRepository repositorio, Clock relogio) {
        this.repositorio = repositorio;
        this.relogio = relogio;
    }

    public OportunidadeView executar(CriarOportunidadeCommand c) {
        Oportunidade nova = Oportunidade.criar(c.clienteId(), c.titulo(), c.valor(), c.dataPrevista(), c.responsavelId(), c.etapa(), relogio.instant());
        return OportunidadeView.de(repositorio.salvar(nova));
    }
}
