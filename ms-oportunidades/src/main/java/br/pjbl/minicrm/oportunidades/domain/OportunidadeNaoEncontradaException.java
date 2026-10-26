package br.pjbl.minicrm.oportunidades.domain;

public class OportunidadeNaoEncontradaException extends DomainException {

    public OportunidadeNaoEncontradaException(long id) {
        super("Oportunidade " + id + " não encontrada");
    }

    @Override
    public String codigo() {
        return "NAO_ENCONTRADO";
    }
}
