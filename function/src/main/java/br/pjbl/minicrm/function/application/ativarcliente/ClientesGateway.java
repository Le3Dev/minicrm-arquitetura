package br.pjbl.minicrm.function.application.ativarcliente;

/**
 * Porta de saída: como o pedido chega ao MS Clientes é detalhe da infraestrutura.
 * Falhas temporárias devem ser lançadas como ErroTransitorioException.
 */
public interface ClientesGateway {

    ResultadoAtivacao ativar(String clienteId, String eventoId, String oportunidadeId);
}
