package br.pjbl.minicrm.clientes.infrastructure.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/** Formato do documento na coleção {@code clientes} do MongoDB Atlas. */
@Document("clientes")
@CompoundIndex(name = "lista", def = "{'excluido': 1, 'responsavelId': 1, 'status': 1}")
public class ClienteDocument {

    @Id
    private String id;
    private String nome;
    private String empresa;
    private String email;
    private String telefone;
    private String status;
    private String responsavelId;
    private boolean excluido;
    /** "nome empresa" em minúsculas e sem acento, para a busca do RF04. */
    @Indexed
    private String textoBusca;
    private List<InteracaoDocument> interacoes;
    private List<String> eventosProcessados;
    private Instant criadoEm;
    private Instant atualizadoEm;
    /** Concorrência otimista: o Spring Data recusa gravar se outra operação mudou o documento. */
    @Version
    private Long versao;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponsavelId() { return responsavelId; }
    public void setResponsavelId(String responsavelId) { this.responsavelId = responsavelId; }
    public boolean isExcluido() { return excluido; }
    public void setExcluido(boolean excluido) { this.excluido = excluido; }
    public String getTextoBusca() { return textoBusca; }
    public void setTextoBusca(String textoBusca) { this.textoBusca = textoBusca; }
    public List<InteracaoDocument> getInteracoes() { return interacoes; }
    public void setInteracoes(List<InteracaoDocument> interacoes) { this.interacoes = interacoes; }
    public List<String> getEventosProcessados() { return eventosProcessados; }
    public void setEventosProcessados(List<String> eventosProcessados) { this.eventosProcessados = eventosProcessados; }
    public Instant getCriadoEm() { return criadoEm; }
    public void setCriadoEm(Instant criadoEm) { this.criadoEm = criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
    public void setAtualizadoEm(Instant atualizadoEm) { this.atualizadoEm = atualizadoEm; }
    public Long getVersao() { return versao; }
    public void setVersao(Long versao) { this.versao = versao; }
}
