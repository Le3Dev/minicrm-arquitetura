package br.pjbl.minicrm.oportunidades.domain;

public class ConcorrenciaException extends DomainException {

    public ConcorrenciaException(Long id) {
        super("A oportunidade " + id + " foi alterada por outra operação; tente novamente");
    }

    @Override
    public String codigo() {
        return "CONCORRENCIA";
    }
}
