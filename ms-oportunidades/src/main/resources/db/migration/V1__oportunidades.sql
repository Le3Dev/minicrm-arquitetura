-- Azure SQL: tabela do funil de vendas (RF07, RF08, RF29)
CREATE TABLE oportunidades (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    cliente_id      VARCHAR(64)    NOT NULL,
    titulo          VARCHAR(160)   NOT NULL,
    etapa           VARCHAR(20)    NOT NULL,
    resultado       VARCHAR(10)    NULL,
    motivo_perda    VARCHAR(20)    NULL,
    valor           DECIMAL(14,2)  NOT NULL,
    data_prevista   DATE           NULL,
    responsavel_id  VARCHAR(64)    NOT NULL,
    criada_em       DATETIME2      NOT NULL,
    atualizada_em   DATETIME2      NOT NULL,
    versao          BIGINT         NOT NULL,
    CONSTRAINT ck_oportunidades_etapa CHECK (etapa IN ('PROSPECCAO', 'CONTATO', 'PROPOSTA', 'FECHADO')),
    CONSTRAINT ck_oportunidades_valor CHECK (valor > 0)
);
CREATE INDEX ix_oportunidades_responsavel ON oportunidades (responsavel_id, etapa);
CREATE INDEX ix_oportunidades_cliente ON oportunidades (cliente_id);
