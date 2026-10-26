package br.pjbl.minicrm.function.application.ativarcliente;

public record ProcessamentoAtivacao(String eventId, String clienteId, String oportunidadeId, ResultadoAtivacao resultado) {

    /** Cliente inexistente não se resolve com nova tentativa: vira pendência no log. */
    public boolean pendente() {
        return resultado == ResultadoAtivacao.NAO_ENCONTRADO;
    }
}
