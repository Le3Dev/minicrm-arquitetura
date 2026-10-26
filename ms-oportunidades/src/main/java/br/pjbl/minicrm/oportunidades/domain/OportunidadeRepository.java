package br.pjbl.minicrm.oportunidades.domain;

import java.util.List;
import java.util.Optional;

/** Porta de persistência (Azure SQL na infraestrutura). */
public interface OportunidadeRepository {

    /** Insere (id nulo) ou atualiza, devolvendo a oportunidade com id e versão. */
    Oportunidade salvar(Oportunidade oportunidade);

    Optional<Oportunidade> buscarPorId(long id);

    /** TODO (aluno B): filtros por cliente e responsáveis para o funil (RF08, RF15). */
    List<Oportunidade> listar();
}
