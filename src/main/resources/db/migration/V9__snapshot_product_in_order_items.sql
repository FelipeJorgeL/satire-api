ALTER TABLE itens_pedidos
    ADD COLUMN produto_id UUID;

UPDATE itens_pedidos item
SET produto_id = variation.produto_id
FROM variacoes_produtos variation
WHERE item.variacao_produto_id = variation.id
  AND item.produto_id IS NULL;

ALTER TABLE itens_pedidos
    ADD CONSTRAINT fk_itens_pedidos_produto FOREIGN KEY (produto_id)
        REFERENCES produtos (id) ON DELETE SET NULL;

CREATE INDEX idx_itens_pedidos_produto_id
    ON itens_pedidos (produto_id);
