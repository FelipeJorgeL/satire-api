ALTER TABLE movimentacoes_estoque
    DROP CONSTRAINT ck_movimentacoes_estoque_tipo,
    DROP CONSTRAINT ck_movimentacoes_estoque_consistencia,
    DROP CONSTRAINT ck_movimentacoes_estoque_pedido;

ALTER TABLE movimentacoes_estoque
    ADD CONSTRAINT ck_movimentacoes_estoque_tipo CHECK (tipo IN (
        'ENTRADA', 'SAIDA', 'VENDA', 'CANCELAMENTO', 'RESERVA', 'LIBERACAO'
    )),
    ADD CONSTRAINT ck_movimentacoes_estoque_consistencia CHECK (
        (tipo IN ('ENTRADA', 'CANCELAMENTO', 'LIBERACAO')
            AND estoque_novo = estoque_anterior + quantidade)
        OR
        (tipo IN ('SAIDA', 'RESERVA')
            AND estoque_novo = estoque_anterior - quantidade)
        OR
        (tipo = 'VENDA' AND (
            estoque_novo = estoque_anterior - quantidade
            OR estoque_novo = estoque_anterior
        ))
    ),
    ADD CONSTRAINT ck_movimentacoes_estoque_pedido CHECK (
        (tipo IN ('VENDA', 'CANCELAMENTO', 'RESERVA', 'LIBERACAO')
            AND pedido_id IS NOT NULL)
        OR
        (tipo IN ('ENTRADA', 'SAIDA') AND pedido_id IS NULL)
    );
