CREATE TABLE rate_limit_buckets (
    chave VARCHAR(80) PRIMARY KEY,
    janela_inicio TIMESTAMPTZ NOT NULL,
    tentativas INTEGER NOT NULL DEFAULT 0,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_rate_limit_buckets_tentativas CHECK (tentativas >= 0)
);

CREATE INDEX idx_rate_limit_buckets_atualizado_em
    ON rate_limit_buckets (atualizado_em);
