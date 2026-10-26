package br.pjbl.minicrm.oportunidades.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Linha da tabela {@code oportunidades} no Azure SQL (criada pela migração Flyway V1). */
@Entity
@Table(name = "oportunidades")
public class OportunidadeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "cliente_id", nullable = false, length = 64)
    private String clienteId;
    @Column(nullable = false, length = 160)
    private String titulo;
    @Column(nullable = false, length = 20)
    private String etapa;
    @Column(length = 10)
    private String resultado;
    @Column(name = "motivo_perda", length = 20)
    private String motivoPerda;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;
    @Column(name = "data_prevista")
    private LocalDate dataPrevista;
    @Column(name = "responsavel_id", nullable = false, length = 64)
    private String responsavelId;
    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;
    @Column(name = "atualizada_em", nullable = false)
    private Instant atualizadaEm;
    @Version
    private Long versao;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String v) { this.clienteId = v; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }
    public String getEtapa() { return etapa; }
    public void setEtapa(String v) { this.etapa = v; }
    public String getResultado() { return resultado; }
    public void setResultado(String v) { this.resultado = v; }
    public String getMotivoPerda() { return motivoPerda; }
    public void setMotivoPerda(String v) { this.motivoPerda = v; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal v) { this.valor = v; }
    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate v) { this.dataPrevista = v; }
    public String getResponsavelId() { return responsavelId; }
    public void setResponsavelId(String v) { this.responsavelId = v; }
    public Instant getCriadaEm() { return criadaEm; }
    public void setCriadaEm(Instant v) { this.criadaEm = v; }
    public Instant getAtualizadaEm() { return atualizadaEm; }
    public void setAtualizadaEm(Instant v) { this.atualizadaEm = v; }
    public Long getVersao() { return versao; }
    public void setVersao(Long v) { this.versao = v; }
}
