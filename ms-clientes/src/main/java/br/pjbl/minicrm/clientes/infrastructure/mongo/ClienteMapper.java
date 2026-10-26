package br.pjbl.minicrm.clientes.infrastructure.mongo;

import br.pjbl.minicrm.clientes.domain.Cliente;
import br.pjbl.minicrm.clientes.domain.Interacao;
import br.pjbl.minicrm.clientes.domain.StatusCliente;
import br.pjbl.minicrm.clientes.domain.TipoInteracao;
import br.pjbl.minicrm.clientes.infrastructure.TextoBusca;

import java.util.List;

/** Converte entre a entidade de domínio e o documento do MongoDB. */
public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static ClienteDocument paraDocumento(Cliente cliente) {
        Cliente.Estado e = cliente.estado();
        ClienteDocument d = new ClienteDocument();
        d.setId(e.id());
        d.setNome(e.nome());
        d.setEmpresa(e.empresa());
        d.setEmail(e.email());
        d.setTelefone(e.telefone());
        d.setStatus(e.status().name());
        d.setResponsavelId(e.responsavelId());
        d.setExcluido(e.excluido());
        d.setTextoBusca(TextoBusca.de(e.nome(), e.empresa()));
        d.setInteracoes(e.interacoes().stream()
                .map(i -> new InteracaoDocument(i.id(), i.tipo().name(), i.obs(), i.data(), i.autorId()))
                .toList());
        d.setEventosProcessados(e.eventosProcessados());
        d.setCriadoEm(e.criadoEm());
        d.setAtualizadoEm(e.atualizadoEm());
        d.setVersao(e.versao());
        return d;
    }

    public static Cliente paraDominio(ClienteDocument d) {
        List<Interacao> interacoes = d.getInteracoes() == null ? List.of() : d.getInteracoes().stream()
                .map(i -> new Interacao(i.id(), TipoInteracao.valueOf(i.tipo()), i.obs(), i.data(), i.autorId()))
                .toList();
        return Cliente.restaurar(new Cliente.Estado(d.getId(), d.getNome(), d.getEmpresa(), d.getEmail(), d.getTelefone(),
                StatusCliente.valueOf(d.getStatus()), d.getResponsavelId(), d.isExcluido(), interacoes,
                d.getEventosProcessados() == null ? List.of() : d.getEventosProcessados(),
                d.getCriadoEm(), d.getAtualizadoEm(), d.getVersao()));
    }
}
