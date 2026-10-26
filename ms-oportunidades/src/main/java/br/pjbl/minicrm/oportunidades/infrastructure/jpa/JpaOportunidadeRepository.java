package br.pjbl.minicrm.oportunidades.infrastructure.jpa;

import br.pjbl.minicrm.oportunidades.domain.ConcorrenciaException;
import br.pjbl.minicrm.oportunidades.domain.Etapa;
import br.pjbl.minicrm.oportunidades.domain.MotivoPerda;
import br.pjbl.minicrm.oportunidades.domain.Oportunidade;
import br.pjbl.minicrm.oportunidades.domain.OportunidadeRepository;
import br.pjbl.minicrm.oportunidades.domain.Resultado;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

/** Adaptador Azure SQL (Spring Data JPA) da porta OportunidadeRepository. */
public class JpaOportunidadeRepository implements OportunidadeRepository {

    private final OportunidadeJpaRepository jpa;

    public JpaOportunidadeRepository(OportunidadeJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Oportunidade salvar(Oportunidade oportunidade) {
        try {
            return paraDominio(jpa.saveAndFlush(paraEntidade(oportunidade.estado())));
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConcorrenciaException(oportunidade.id());
        }
    }

    @Override
    public Optional<Oportunidade> buscarPorId(long id) {
        return jpa.findById(id).map(JpaOportunidadeRepository::paraDominio);
    }

    @Override
    public List<Oportunidade> listar() {
        return jpa.findAll().stream().map(JpaOportunidadeRepository::paraDominio).toList();
    }

    static OportunidadeEntity paraEntidade(Oportunidade.Estado e) {
        OportunidadeEntity x = new OportunidadeEntity();
        x.setId(e.id());
        x.setClienteId(e.clienteId());
        x.setTitulo(e.titulo());
        x.setEtapa(e.etapa().name());
        x.setResultado(e.resultado() == null ? null : e.resultado().name());
        x.setMotivoPerda(e.motivoPerda() == null ? null : e.motivoPerda().name());
        x.setValor(e.valor());
        x.setDataPrevista(e.dataPrevista());
        x.setResponsavelId(e.responsavelId());
        x.setCriadaEm(e.criadaEm());
        x.setAtualizadaEm(e.atualizadaEm());
        x.setVersao(e.versao());
        return x;
    }

    static Oportunidade paraDominio(OportunidadeEntity x) {
        return Oportunidade.restaurar(new Oportunidade.Estado(x.getId(), x.getClienteId(), x.getTitulo(), Etapa.valueOf(x.getEtapa()),
                x.getResultado() == null ? null : Resultado.valueOf(x.getResultado()),
                x.getMotivoPerda() == null ? null : MotivoPerda.valueOf(x.getMotivoPerda()),
                x.getValor(), x.getDataPrevista(), x.getResponsavelId(), x.getCriadaEm(), x.getAtualizadaEm(), x.getVersao()));
    }
}
