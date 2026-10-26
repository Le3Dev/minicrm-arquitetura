package br.pjbl.minicrm.oportunidades.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Oportunidade do funil de vendas (RF07, RF08, RF09, RF29). Java puro.
 *
 * <p>Regras da seção 5.2 da SPEC do Mini CRM:
 * <ul>
 *   <li>Mover para Fechado exige resultado (ganho ou perdido); perdido exige motivo da lista fixa.</li>
 *   <li>Tirar do Fechado exige {@code reabrir = true}; ao reabrir, resultado e motivo são limpos.</li>
 *   <li>Entre etapas abertas o movimento é livre, inclusive para trás.</li>
 *   <li>Fechar como ganho gera o evento {@link OportunidadeGanha}.</li>
 * </ul>
 */
public final class Oportunidade {

    private Long id;
    private final String clienteId;
    private final String titulo;
    private Etapa etapa;
    private Resultado resultado;
    private MotivoPerda motivoPerda;
    private final BigDecimal valor;
    private final LocalDate dataPrevista;
    private final String responsavelId;
    private final Instant criadaEm;
    private Instant atualizadaEm;
    private Long versao;

    public record Estado(Long id, String clienteId, String titulo, Etapa etapa, Resultado resultado, MotivoPerda motivoPerda,
                         BigDecimal valor, LocalDate dataPrevista, String responsavelId, Instant criadaEm, Instant atualizadaEm, Long versao) {
    }

    private Oportunidade(Estado e) {
        this.id = e.id();
        this.clienteId = e.clienteId();
        this.titulo = e.titulo();
        this.etapa = e.etapa();
        this.resultado = e.resultado();
        this.motivoPerda = e.motivoPerda();
        this.valor = e.valor();
        this.dataPrevista = e.dataPrevista();
        this.responsavelId = e.responsavelId();
        this.criadaEm = e.criadaEm();
        this.atualizadaEm = e.atualizadaEm();
        this.versao = e.versao();
    }

    /** RF07: cria vinculada a um cliente; começa em Prospecção, salvo etapa aberta informada. */
    public static Oportunidade criar(String clienteId, String titulo, BigDecimal valor, LocalDate dataPrevista, String responsavelId,
                                     String etapaInicial, Instant agora) {
        List<String> erros = new ArrayList<>();
        if (vazio(clienteId)) erros.add("clienteId: obrigatório");
        String t = titulo == null ? "" : titulo.trim();
        if (t.length() < 3 || t.length() > 160) erros.add("titulo: deve ter entre 3 e 160 caracteres");
        if (valor == null || valor.signum() <= 0) erros.add("valor: deve ser maior que zero");
        if (vazio(responsavelId)) erros.add("responsavelId: obrigatório");
        Etapa etapa = Etapa.PROSPECCAO;
        if (etapaInicial != null && !etapaInicial.isBlank()) {
            etapa = Etapa.deTexto(etapaInicial).orElse(null);
            if (etapa == null) erros.add("etapa: deve ser Prospecção, Contato ou Proposta");
            else if (etapa == Etapa.FECHADO) erros.add("etapa: uma oportunidade nova não pode nascer fechada");
        }
        if (!erros.isEmpty()) throw new ValidacaoException("Oportunidade inválida", erros);
        return new Oportunidade(new Estado(null, clienteId.trim(), t, etapa, null, null, valor.setScale(2, RoundingMode.HALF_UP),
                dataPrevista, responsavelId.trim(), agora, agora, null));
    }

    public static Oportunidade restaurar(Estado estado) {
        return new Oportunidade(estado);
    }

    /**
     * RF09: move no funil. Devolve o evento OportunidadeGanha quando a movimentação fecha como ganho.
     */
    public Optional<OportunidadeGanha> mover(String novaEtapaTexto, String resultadoTexto, String motivoTexto, boolean reabrir, Instant agora) {
        Etapa nova = Etapa.deTexto(novaEtapaTexto).orElseThrow(() -> new ValidacaoException("etapa: deve ser Prospecção, Contato, Proposta ou Fechado"));

        if (etapa == Etapa.FECHADO) {
            if (nova == Etapa.FECHADO) {
                throw new MovimentacaoInvalidaException("A oportunidade já está fechada; reabra antes de fechar de novo");
            }
            if (!reabrir) {
                throw new MovimentacaoInvalidaException("Oportunidade fechada só sai do Fechado com reabrir = true");
            }
            this.resultado = null;
            this.motivoPerda = null;
            this.etapa = nova;
            this.atualizadaEm = agora;
            return Optional.empty();
        }

        if (nova != Etapa.FECHADO) {
            this.etapa = nova;
            this.atualizadaEm = agora;
            return Optional.empty();
        }

        Resultado r = Resultado.deTexto(resultadoTexto)
                .orElseThrow(() -> new ValidacaoException("resultado: obrigatório ao fechar (ganho ou perdido)"));
        MotivoPerda motivo = null;
        if (r == Resultado.PERDIDO) {
            motivo = MotivoPerda.deTexto(motivoTexto)
                    .orElseThrow(() -> new ValidacaoException("motivoPerda: obrigatório ao perder (Preço, Concorrente, Sem verba, Sem resposta)"));
        }
        this.etapa = Etapa.FECHADO;
        this.resultado = r;
        this.motivoPerda = motivo;
        this.atualizadaEm = agora;
        if (r == Resultado.GANHO) {
            if (id == null) {
                throw new IllegalStateException("Grave a oportunidade antes de fechá-la");
            }
            return Optional.of(new OportunidadeGanha(UUID.randomUUID().toString(), id, clienteId, valor, responsavelId, agora));
        }
        return Optional.empty();
    }

    public Estado estado() {
        return new Estado(id, clienteId, titulo, etapa, resultado, motivoPerda, valor, dataPrevista, responsavelId, criadaEm, atualizadaEm, versao);
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    public Long id() { return id; }
    public String clienteId() { return clienteId; }
    public String titulo() { return titulo; }
    public Etapa etapa() { return etapa; }
    public Resultado resultado() { return resultado; }
    public MotivoPerda motivoPerda() { return motivoPerda; }
    public BigDecimal valor() { return valor; }
    public LocalDate dataPrevista() { return dataPrevista; }
    public String responsavelId() { return responsavelId; }
    public Instant criadaEm() { return criadaEm; }
    public Instant atualizadaEm() { return atualizadaEm; }
    public Long versao() { return versao; }
}
