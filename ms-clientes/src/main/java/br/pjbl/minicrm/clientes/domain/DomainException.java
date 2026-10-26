package br.pjbl.minicrm.clientes.domain;

/**
 * Erro de regra de negócio. Não conhece HTTP: a camada api traduz o
 * {@link #codigo()} para um status (ver api.comum.TratadorDeErros).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String mensagem) {
        super(mensagem);
    }

    public abstract String codigo();
}
