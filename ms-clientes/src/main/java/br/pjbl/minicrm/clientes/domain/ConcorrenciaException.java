package br.pjbl.minicrm.clientes.domain;

/** Outra operação alterou o cliente entre a leitura e a gravação (concorrência otimista). */
public class ConcorrenciaException extends DomainException {

    public ConcorrenciaException(String clienteId) {
        super("O cliente " + clienteId + " foi alterado por outra operação; tente novamente");
    }

    @Override
    public String codigo() {
        return "CONCORRENCIA";
    }
}
