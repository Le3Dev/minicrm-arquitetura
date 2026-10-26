package br.pjbl.minicrm.clientes.domain;

import java.util.Optional;

/**
 * Porta de saída (Clean Architecture): o domínio diz o que precisa,
 * a infraestrutura (MongoDB ou memória) diz como.
 */
public interface ClienteRepository {

    /** Insere (versão nula) ou atualiza. Lança {@link ConcorrenciaException} se a versão gravada mudou. */
    void salvar(Cliente cliente);

    Optional<Cliente> buscarPorId(String id);

    /** Pesquisa paginada que nunca devolve clientes excluídos (RF03, RF04, RF18). */
    Pagina<Cliente> pesquisar(FiltroClientes filtro);
}
