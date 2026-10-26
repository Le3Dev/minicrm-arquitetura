package br.pjbl.minicrm.clientes.domain;

/** O que aconteceu ao processar uma OportunidadeGanha para o cliente. */
public enum ResultadoAtivacao {
    /** Era Prospect e passou a Ativo. */
    ATIVADO,
    /** Já era Ativo; o evento foi registrado e nada mais mudou. */
    JA_ATIVO,
    /** O mesmo evento já tinha sido processado (entrega repetida do Service Bus). */
    REPETIDO,
    /** Cliente excluído: o histórico é preservado e o status não volta a Ativo. */
    IGNORADO_EXCLUIDO
}
