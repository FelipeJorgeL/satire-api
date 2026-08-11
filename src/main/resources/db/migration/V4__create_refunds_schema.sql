CREATE TABLE estornos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pagamento_id UUID NOT NULL UNIQUE,
    usuario_id UUID NOT NULL,
    chave_idempotencia VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'SOLICITADO',
    valor NUMERIC(12, 2) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    transacao_gateway_id VARCHAR(255) UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_estornos_status CHECK (status IN (
        'SOLICITADO', 'PROCESSANDO', 'CONCLUIDO', 'RECUSADO', 'CANCELADO'
    )),
    CONSTRAINT ck_estornos_valor CHECK (valor > 0),
    CONSTRAINT ck_estornos_motivo CHECK (char_length(trim(motivo)) >= 3),
    CONSTRAINT fk_estornos_pagamento FOREIGN KEY (pagamento_id)
        REFERENCES pagamentos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_estornos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE INDEX idx_estornos_status ON estornos (status);
