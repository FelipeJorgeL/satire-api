CREATE TABLE eventos_webhook_pagamentos (
    evento_id VARCHAR(100) PRIMARY KEY,
    pagamento_id UUID NOT NULL,
    hash_payload VARCHAR(64) NOT NULL,
    recebido_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_eventos_webhook_pagamentos_evento CHECK (
        evento_id ~ '^[A-Za-z0-9._:-]{8,100}$'
    ),
    CONSTRAINT ck_eventos_webhook_pagamentos_hash CHECK (
        hash_payload ~ '^[0-9a-f]{64}$'
    ),
    CONSTRAINT fk_eventos_webhook_pagamentos_pagamento FOREIGN KEY (pagamento_id)
        REFERENCES pagamentos (id) ON DELETE RESTRICT
);

CREATE INDEX idx_eventos_webhook_pagamentos_pagamento
    ON eventos_webhook_pagamentos (pagamento_id, recebido_em DESC);
