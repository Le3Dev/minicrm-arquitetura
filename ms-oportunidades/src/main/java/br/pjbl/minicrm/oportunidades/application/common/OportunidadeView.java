package br.pjbl.minicrm.oportunidades.application.common;

import br.pjbl.minicrm.oportunidades.domain.Oportunidade;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Saída dos casos de uso, nos mesmos nomes de campo do Mini CRM original. */
public record OportunidadeView(long id, String clienteId, String titulo, String etapa, String resultado, String motivoPerda,
                               BigDecimal valor, LocalDate dataPrevista, String responsavelId, Instant criadaEm, Instant atualizada) {

    public static OportunidadeView de(Oportunidade o) {
        return new OportunidadeView(o.id(), o.clienteId(), o.titulo(), o.etapa().rotulo(),
                o.resultado() == null ? null : o.resultado().rotulo(),
                o.motivoPerda() == null ? null : o.motivoPerda().rotulo(),
                o.valor(), o.dataPrevista(), o.responsavelId(), o.criadaEm(), o.atualizadaEm());
    }
}
