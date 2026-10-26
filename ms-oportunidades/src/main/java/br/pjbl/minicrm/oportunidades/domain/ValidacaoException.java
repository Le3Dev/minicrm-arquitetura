package br.pjbl.minicrm.oportunidades.domain;

import java.util.List;

/** Entrada inválida, com a lista completa de problemas (mesmo formato do MS Clientes). */
public class ValidacaoException extends DomainException {

    private final List<String> erros;

    public ValidacaoException(String mensagem, List<String> erros) {
        super(mensagem);
        this.erros = List.copyOf(erros);
    }

    public ValidacaoException(String erro) {
        this("Dados inválidos", List.of(erro));
    }

    public List<String> erros() {
        return erros;
    }

    @Override
    public String codigo() {
        return "VALIDACAO";
    }
}
