CREATE TABLE reservas_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL UNIQUE,
    usuario_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    expira_em TIMESTAMPTZ NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_reservas_estoque_status CHECK (
        status IN ('ACTIVE', 'RELEASED', 'CONFIRMED', 'EXPIRED')
    ),
    CONSTRAINT fk_reservas_estoque_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservas_estoque_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE TABLE itens_reservas_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reserva_id UUID NOT NULL,
    variacao_produto_id UUID NOT NULL,
    quantidade INTEGER NOT NULL,
    CONSTRAINT ck_itens_reservas_estoque_quantidade CHECK (quantidade > 0),
    CONSTRAINT uk_itens_reservas_estoque_variacao
        UNIQUE (reserva_id, variacao_produto_id),
    CONSTRAINT fk_itens_reservas_estoque_reserva FOREIGN KEY (reserva_id)
        REFERENCES reservas_estoque (id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_reservas_estoque_variacao FOREIGN KEY (variacao_produto_id)
        REFERENCES variacoes_produtos (id) ON DELETE RESTRICT
);

CREATE INDEX idx_reservas_estoque_expiracao
    ON reservas_estoque (status, expira_em);
