package br.pjbl.minicrm.oportunidades.application.moveroportunidade;

/** RF09/RF29: {@code resultado} e {@code motivoPerda} só valem ao fechar; {@code reabrir} para sair do Fechado. */
public record MoverOportunidadeCommand(long id, String etapa, String resultado, String motivoPerda, boolean reabrir) {
}
