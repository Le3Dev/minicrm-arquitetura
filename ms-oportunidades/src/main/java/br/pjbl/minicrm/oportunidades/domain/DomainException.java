package br.pjbl.minicrm.oportunidades.domain;

/** Erro de regra de negócio; a camada api traduz o código para status HTTP. */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensagem) {
        super(mensagem);
    }

    public abstract String codigo();
}
