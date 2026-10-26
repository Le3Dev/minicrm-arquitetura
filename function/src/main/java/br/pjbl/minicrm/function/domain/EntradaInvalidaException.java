package br.pjbl.minicrm.function.domain;

/** A entrada não tem o formato esperado; repetir não adianta. */
public class EntradaInvalidaException extends RuntimeException {

    public EntradaInvalidaException(String mensagem) {
        super(mensagem);
    }
}
