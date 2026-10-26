package br.pjbl.minicrm.clientes.infrastructure.memoria;

import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.ClienteRepository;
import br.pjbl.minicrm.clientes.domain.ConcorrenciaException;
import br.pjbl.minicrm.clientes.domain.FiltroClientes;
import br.pjbl.minicrm.clientes.domain.Pagina;
import br.pjbl.minicrm.clientes.infrastructure.TextoBusca;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador em memória da porta ClienteRepository: usado nos testes e para rodar
 * o serviço sem banco (perfil "memoria"). Segue as mesmas regras de versão do adaptador MongoDB.
 */
public class InMemoryClienteRepository implements ClienteRepository {

    private final Map<String, Cliente.Estado> registros = new ConcurrentHashMap<>();

    @Override
    public synchronized void salvar(Cliente cliente) {
        Cliente.Estado atual = registros.get(cliente.id());
        Long versaoGravada = atual == null ? null : atual.versao();
        if (!Objects.equals(versaoGravada, cliente.versao())) {
            throw new ConcorrenciaException(cliente.id());
        }
        long novaVersao = cliente.versao() == null ? 0 : cliente.versao() + 1;
        Cliente.Estado e = cliente.estado();
        registros.put(cliente.id(), new Cliente.Estado(e.id(), e.nome(), e.empresa(), e.email(), e.telefone(), e.status(),
                e.responsavelId(), e.excluido(), e.interacoes(), e.eventosProcessados(), e.criadoEm(), e.atualizadoEm(), novaVersao));
        cliente.marcarGravado(novaVersao);
    }

    @Override
    public Optional<Cliente> buscarPorId(String id) {
        return Optional.ofNullable(registros.get(id)).map(Cliente::restaurar);
    }

    @Override
    public Pagina<Cliente> pesquisar(FiltroClientes f) {
        String termo = f.texto() == null ? null : TextoBusca.normalizar(f.texto());
        List<Cliente.Estado> encontrados = registros.values().stream()
                .filter(e -> !e.excluido())
                .filter(e -> termo == null || TextoBusca.de(e.nome(), e.empresa()).contains(termo))
                .filter(e -> f.status() == null || e.status() == f.status())
                .filter(e -> f.responsaveis().isEmpty() || f.responsaveis().contains(e.responsavelId()))
                .filter(e -> f.ids().isEmpty() || f.ids().contains(e.id()))
                .sorted(Comparator.comparing((Cliente.Estado e) -> TextoBusca.normalizar(e.nome())).thenComparing(Cliente.Estado::id))
                .toList();
        List<Cliente> pagina = encontrados.stream().skip(f.deslocamento()).limit(f.porPagina()).map(Cliente::restaurar).toList();
        return new Pagina<>(pagina, f.pagina(), f.porPagina(), encontrados.size());
    }
}
