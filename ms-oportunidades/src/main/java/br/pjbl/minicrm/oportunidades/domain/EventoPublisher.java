package br.pjbl.minicrm.oportunidades.domain;

/** Porta de saída para eventos de integração (Azure Service Bus na infraestrutura). */
public interface EventoPublisher {

    void publicar(OportunidadeGanha evento);
}
