package br.pjbl.minicrm.function.domain;

/** Falha temporária (rede, timeout, 5xx). Lançada para o Service Bus entregar a mensagem de novo. */
public class ErroTransitorioException extends RuntimeException {

    public ErroTransitorioException(String mensagem) {
        super(mensagem);
    }

    public ErroTransitorioException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
