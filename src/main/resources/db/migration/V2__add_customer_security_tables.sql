-- Estruturas de autenticação e outbox de confirmação de e-mail.
-- A migration é segura para reexecução manual; o Flyway continua controlando a versão.

ALTER TABLE usuarios
    ALTER COLUMN ativo SET DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS refresh_tokens (
    usuario_id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS confirmacoes_email (
    usuario_id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    consumido_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_confirmacoes_email_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

ALTER TABLE confirmacoes_email
    ADD COLUMN IF NOT EXISTS consumido_em TIMESTAMPTZ;

CREATE TABLE IF NOT EXISTS confirmacoes_email_outbox (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    destinatario VARCHAR(255) NOT NULL,
    link_cifrado TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    tentativas INTEGER NOT NULL DEFAULT 0,
    proxima_tentativa_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultima_tentativa_em TIMESTAMPTZ,
    ultima_falha VARCHAR(500),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_confirmacoes_email_outbox_status
        CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'FAILED')),
    CONSTRAINT ck_confirmacoes_email_outbox_tentativas
        CHECK (tentativas >= 0),
    CONSTRAINT fk_confirmacoes_email_outbox_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_refresh_tokens_hash
    ON refresh_tokens (token_hash);
CREATE UNIQUE INDEX IF NOT EXISTS idx_confirmacoes_email_hash
    ON confirmacoes_email (token_hash);
CREATE INDEX IF NOT EXISTS idx_confirmacoes_email_outbox_ready
    ON confirmacoes_email_outbox (status, proxima_tentativa_em);
CREATE INDEX IF NOT EXISTS idx_confirmacoes_email_outbox_usuario
    ON confirmacoes_email_outbox (usuario_id);
