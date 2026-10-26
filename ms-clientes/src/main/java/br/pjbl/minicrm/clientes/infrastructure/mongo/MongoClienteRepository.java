package br.pjbl.minicrm.clientes.infrastructure.mongo;

import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.ConcorrenciaException;
import br.pjbl.minicrm.clientes.domain.FiltroClientes;
import br.pjbl.minicrm.clientes.domain.Pagina;
import br.pjbl.minicrm.clientes.infrastructure.TextoBusca;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Adaptador MongoDB Atlas da porta ClienteRepository. */
public class MongoClienteRepository implements ClienteRepository {

    private final MongoTemplate mongo;

    public MongoClienteRepository(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void salvar(Cliente cliente) {
        ClienteDocument documento = ClienteMapper.paraDocumento(cliente);
        try {
            ClienteDocument gravado = cliente.versao() == null ? mongo.insert(documento) : mongo.save(documento);
            cliente.marcarGravado(gravado.getVersao());
        } catch (OptimisticLockingFailureException | DuplicateKeyException e) {
            throw new ConcorrenciaException(cliente.id());
        }
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        return Optional.ofNullable(mongo.findById(id, ClienteDocument.class)).map(ClienteMapper::paraDominio);
    }

    @Override
    public Pagina<Cliente> pesquisar(FiltroClientes f) {
        Criteria criterio = Criteria.where("excluido").is(false);
        if (f.texto() != null) {
            criterio = criterio.and("textoBusca").regex(Pattern.quote(TextoBusca.normalizar(f.texto())));
        }
        if (f.status() != null) {
            criterio = criterio.and("status").is(f.status().name());
        }
        if (!f.responsaveis().isEmpty()) {
            criterio = criterio.and("responsavelId").in(f.responsaveis());
        }
        if (!f.ids().isEmpty()) {
            criterio = criterio.and("_id").in(f.ids());
        }
        long total = mongo.count(new Query(criterio), ClienteDocument.class);
        Query pagina = new Query(criterio)
                .with(Sort.by("textoBusca", "_id"))
                .skip(f.deslocamento())
                .limit(f.porPagina());
        List<Cliente> itens = mongo.find(pagina, ClienteDocument.class).stream().map(ClienteMapper::paraDominio).toList();
        return new Pagina<>(itens, f.pagina(), f.porPagina(), total);
    }
}
