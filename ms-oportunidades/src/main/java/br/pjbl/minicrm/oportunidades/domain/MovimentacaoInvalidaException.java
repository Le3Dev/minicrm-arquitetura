package br.pjbl.minicrm.oportunidades.domain;

/** RF09: movimentação que o funil não permite (ex.: tirar do Fechado sem reabrir). */
public class MovimentacaoInvalidaException extends DomainException {

    public MovimentacaoInvalidaException(String mensagem) {
        super(mensagem);
    }

    @Override
    public String codigo() {
        return "MOVIMENTACAO_INVALIDA";
    }
}
