package br.pjbl.minicrm.clientes.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Entidade Cliente do Mini CRM. Toda regra de negócio de cliente mora aqui,
 * sem depender de Spring, MongoDB ou HTTP.
 *
 * <ul>
 *   <li>RF03/RF30: nasce Prospect; a exclusão é lógica (vira Inativo e some das listagens).</li>
 *   <li>RF30: passa a Ativo na primeira oportunidade ganha (evento OportunidadeGanha).</li>
 *   <li>RF05/RF06: guarda o histórico de interações.</li>
 *   <li>RF26: tem exatamente um responsável, transferível.</li>
 * </ul>
 */
public final class Cliente {

    /** Quantos eventos já processados o cliente lembra, para ignorar entregas repetidas. */
    public static final int LIMITE_EVENTOS_LEMBRADOS = 50;

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private final String id;
    private String nome;
    private String empresa;
    private String email;
    private String telefone;
    private StatusCliente status;
    private String responsavelId;
    private boolean excluido;
    private final List<Interacao> interacoes;
    private final List<String> eventosProcessados;
    private final Instant criadoEm;
    private Instant atualizadoEm;
    /** Versão para concorrência otimista; {@code null} = ainda não gravado. */
    private Long versao;

    private Cliente(Estado e) {
        this.id = Objects.requireNonNull(e.id());
        this.nome = e.nome();
        this.empresa = e.empresa();
        this.email = e.email();
        this.telefone = e.telefone() == null ? "" : e.telefone();
        this.status = Objects.requireNonNull(e.status());
        this.responsavelId = e.responsavelId();
        this.excluido = e.excluido();
        this.interacoes = new ArrayList<>(e.interacoes() == null ? List.of() : e.interacoes());
        this.eventosProcessados = new ArrayList<>(e.eventosProcessados() == null ? List.of() : e.eventosProcessados());
        this.criadoEm = Objects.requireNonNull(e.criadoEm());
        this.atualizadoEm = Objects.requireNonNull(e.atualizadoEm());
        this.versao = e.versao();
    }

    /** Estado completo, usado para gravar e reconstruir o cliente (repositórios). */
    public record Estado(
            String id, String nome, String empresa, String email, String telefone, StatusCliente status,
            String responsavelId, boolean excluido, List<Interacao> interacoes, List<String> eventosProcessados,
            Instant criadoEm, Instant atualizadoEm, Long versao) {
    }

    public static Cliente cadastrar(DadosCliente dados, Instant agora) {
        List<String> erros = new ArrayList<>();
        String nome = validarNome(dados.nome(), erros);
        String empresa = validarEmpresa(dados.empresa(), erros);
        String email = validarEmail(dados.email(), erros);
        String telefone = validarTelefone(dados.telefone(), erros);
        String responsavel = validarResponsavel(dados.responsavelId(), erros);
        lancarSeHouver("Cliente inválido", erros);
        return new Cliente(new Estado(UUID.randomUUID().toString(), nome, empresa, email, telefone, StatusCliente.PROSPECT,
                responsavel, false, List.of(), List.of(), agora, agora, null));
    }

    /** Reconstrói um cliente já gravado, sem revalidar. Uso exclusivo dos repositórios. */
    public static Cliente restaurar(Estado estado) {
        return new Cliente(estado);
    }

    /** PATCH: altera só o que veio preenchido. Inativo só por exclusão. */
    public void alterar(AlteracaoCliente alteracao, Instant agora) {
        exigirNaoExcluido();
        if (alteracao.vazia()) {
            throw new ValidacaoException("Envie ao menos um campo para alterar");
        }
        List<String> erros = new ArrayList<>();
        String novoNome = alteracao.nome() == null ? nome : validarNome(alteracao.nome(), erros);
        String novaEmpresa = alteracao.empresa() == null ? empresa : validarEmpresa(alteracao.empresa(), erros);
        String novoEmail = alteracao.email() == null ? email : validarEmail(alteracao.email(), erros);
        String novoTelefone = alteracao.telefone() == null ? telefone : validarTelefone(alteracao.telefone(), erros);
        String novoResponsavel = alteracao.responsavelId() == null ? responsavelId : validarResponsavel(alteracao.responsavelId(), erros);
        StatusCliente novoStatus = status;
        if (alteracao.status() != null) {
            novoStatus = StatusCliente.deTexto(alteracao.status()).orElse(null);
            if (novoStatus == null) {
                erros.add("status: deve ser Prospect ou Ativo");
            } else if (novoStatus == StatusCliente.INATIVO) {
                erros.add("status: para inativar, exclua o cliente (exclusão lógica)");
            }
        }
        lancarSeHouver("Cliente inválido", erros);
        this.nome = novoNome;
        this.empresa = novaEmpresa;
        this.email = novoEmail;
        this.telefone = novoTelefone;
        this.responsavelId = novoResponsavel;
        this.status = novoStatus;
        this.atualizadoEm = agora;
    }

    /** RF03: exclusão lógica. Excluir de novo não muda nada. */
    public void excluir(Instant agora) {
        if (excluido) {
            return;
        }
        this.excluido = true;
        this.status = StatusCliente.INATIVO;
        this.atualizadoEm = agora;
    }

    /** RF05: registra uma interação no histórico do cliente. */
    public void registrarInteracao(Interacao interacao, Instant agora) {
        exigirNaoExcluido();
        interacoes.add(Objects.requireNonNull(interacao));
        this.atualizadoEm = agora;
    }

    /**
     * RF30, disparado pelo evento OportunidadeGanha: Prospect passa a Ativo.
     * Idempotente pelo {@code eventoId}: uma segunda entrega do mesmo evento devolve REPETIDO.
     */
    public ResultadoAtivacao ativarPorOportunidadeGanha(String eventoId, Instant agora) {
        if (eventoId == null || eventoId.isBlank()) {
            throw new ValidacaoException("eventoId: obrigatório");
        }
        if (eventosProcessados.contains(eventoId)) {
            return ResultadoAtivacao.REPETIDO;
        }
        if (excluido) {
            return ResultadoAtivacao.IGNORADO_EXCLUIDO;
        }
        eventosProcessados.add(eventoId);
        if (eventosProcessados.size() > LIMITE_EVENTOS_LEMBRADOS) {
            eventosProcessados.remove(0);
        }
        this.atualizadoEm = agora;
        if (status == StatusCliente.ATIVO) {
            return ResultadoAtivacao.JA_ATIVO;
        }
        this.status = StatusCliente.ATIVO;
        return ResultadoAtivacao.ATIVADO;
    }

    /** Chamado pelo repositório depois de gravar com sucesso. */
    public void marcarGravado(long novaVersao) {
        this.versao = novaVersao;
    }

    public Estado estado() {
        return new Estado(id, nome, empresa, email, telefone, status, responsavelId, excluido, List.copyOf(interacoes),
                List.copyOf(eventosProcessados), criadoEm, atualizadoEm, versao);
    }

    private void exigirNaoExcluido() {
        if (excluido) {
            throw new ClienteExcluidoException(id);
        }
    }

    // ---- validações (mesmas regras da seção 5.5 da SPEC do Mini CRM) ----

    private static String validarNome(String valor, List<String> erros) {
        String v = limpar(valor);
        if (v.length() < 3 || v.length() > 120) {
            erros.add("nome: deve ter entre 3 e 120 caracteres");
        }
        return v;
    }

    private static String validarEmpresa(String valor, List<String> erros) {
        String v = limpar(valor);
        if (v.length() < 2 || v.length() > 120) {
            erros.add("empresa: deve ter entre 2 e 120 caracteres");
        }
        return v;
    }

    private static String validarEmail(String valor, List<String> erros) {
        String v = limpar(valor).toLowerCase();
        if (!EMAIL.matcher(v).matches() || v.length() > 160) {
            erros.add("email: formato inválido");
        }
        return v;
    }

    private static String validarTelefone(String valor, List<String> erros) {
        String v = limpar(valor);
        if (v.length() > 30) {
            erros.add("telefone: deve ter no máximo 30 caracteres");
        }
        return v;
    }

    private static String validarResponsavel(String valor, List<String> erros) {
        String v = limpar(valor);
        if (v.isEmpty()) {
            erros.add("responsavelId: obrigatório");
        }
        return v;
    }

    private static String limpar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static void lancarSeHouver(String mensagem, List<String> erros) {
        if (!erros.isEmpty()) {
            throw new ValidacaoException(mensagem, erros);
        }
    }

    // ---- leitura ----

    public String id() { return id; }
    public String nome() { return nome; }
    public String empresa() { return empresa; }
    public String email() { return email; }
    public String telefone() { return telefone; }
    public StatusCliente status() { return status; }
    public String responsavelId() { return responsavelId; }
    public boolean excluido() { return excluido; }
    public List<Interacao> interacoes() { return Collections.unmodifiableList(interacoes); }
    public Instant criadoEm() { return criadoEm; }
    public Instant atualizadoEm() { return atualizadoEm; }
    public Long versao() { return versao; }
}
