-- Modelo lógico de e-commerce para PostgreSQL e importação no DrawDB.
-- Convenções: snake_case, tabelas no plural e chaves estrangeiras como entidade_id.
-- Datas da API usam TIMESTAMPTZ. A aplicação deve atualizar atualizado_em a cada alteração.

CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL,
    senha_hash VARCHAR(255) NOT NULL,
    cpf CHAR(11) UNIQUE,
    telefone VARCHAR(20),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    excluido_em TIMESTAMPTZ,
    CONSTRAINT ck_usuarios_nome CHECK (char_length(trim(nome)) >= 2),
    CONSTRAINT ck_usuarios_email CHECK (position('@' IN email) > 1),
    CONSTRAINT ck_usuarios_email_normalizado CHECK (
        email = LOWER(BTRIM(email))
    ),
    CONSTRAINT ck_usuarios_cpf CHECK (cpf IS NULL OR cpf ~ '^[0-9]{11}$'),
    CONSTRAINT ck_usuarios_exclusao CHECK (excluido_em IS NULL OR ativo = FALSE)
);

CREATE TABLE perfis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(30) NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_perfis_nome CHECK (nome IN ('CLIENTE', 'ADMIN'))
);

CREATE TABLE usuarios_perfis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    perfil_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuarios_perfis UNIQUE (usuario_id, perfil_id),
    CONSTRAINT fk_usuarios_perfis_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_usuarios_perfis_perfil FOREIGN KEY (perfil_id)
        REFERENCES perfis (id) ON DELETE RESTRICT
);

CREATE TABLE enderecos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    apelido VARCHAR(50),
    destinatario VARCHAR(120) NOT NULL,
    cep CHAR(8) NOT NULL,
    rua VARCHAR(180) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(120),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_enderecos_cep CHECK (cep ~ '^[0-9]{8}$'),
    CONSTRAINT ck_enderecos_uf CHECK (uf ~ '^[A-Z]{2}$'),
    CONSTRAINT fk_enderecos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE categorias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    slug VARCHAR(120) NOT NULL UNIQUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    excluido_em TIMESTAMPTZ,
    CONSTRAINT ck_categorias_nome CHECK (char_length(trim(nome)) >= 2),
    CONSTRAINT ck_categorias_slug CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CONSTRAINT ck_categorias_exclusao CHECK (excluido_em IS NULL OR ativo = FALSE)
);

CREATE TABLE produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    categoria_id UUID NOT NULL,
    nome VARCHAR(180) NOT NULL,
    slug VARCHAR(200) NOT NULL UNIQUE,
    descricao TEXT,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    excluido_em TIMESTAMPTZ,
    CONSTRAINT ck_produtos_nome CHECK (char_length(trim(nome)) >= 2),
    CONSTRAINT ck_produtos_slug CHECK (slug ~ '^[a-z0-9]+(?:-[a-z0-9]+)*$'),
    CONSTRAINT ck_produtos_exclusao CHECK (excluido_em IS NULL OR ativo = FALSE),
    CONSTRAINT fk_produtos_categoria FOREIGN KEY (categoria_id)
        REFERENCES categorias (id) ON DELETE RESTRICT
);

CREATE TABLE variacoes_produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    sku VARCHAR(60) NOT NULL UNIQUE,
    nome VARCHAR(120) NOT NULL,
    preco NUMERIC(12, 2) NOT NULL,
    estoque INTEGER NOT NULL DEFAULT 0,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    excluido_em TIMESTAMPTZ,
    CONSTRAINT ck_variacoes_produtos_sku CHECK (char_length(trim(sku)) >= 3),
    CONSTRAINT ck_variacoes_produtos_preco CHECK (preco >= 0),
    CONSTRAINT ck_variacoes_produtos_estoque CHECK (estoque >= 0),
    CONSTRAINT ck_variacoes_produtos_exclusao CHECK (excluido_em IS NULL OR ativo = FALSE),
    CONSTRAINT fk_variacoes_produtos_produto FOREIGN KEY (produto_id)
        REFERENCES produtos (id) ON DELETE CASCADE
);

CREATE TABLE imagens_produtos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    produto_id UUID NOT NULL,
    url TEXT NOT NULL,
    texto_alternativo VARCHAR(255),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    ordem_exibicao INTEGER NOT NULL DEFAULT 0,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_imagens_produtos_url CHECK (char_length(trim(url)) > 0),
    CONSTRAINT ck_imagens_produtos_ordem CHECK (ordem_exibicao >= 0),
    CONSTRAINT fk_imagens_produtos_produto FOREIGN KEY (produto_id)
        REFERENCES produtos (id) ON DELETE CASCADE
);

CREATE TABLE favoritos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_favoritos_usuario_produto UNIQUE (usuario_id, produto_id),
    CONSTRAINT fk_favoritos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_favoritos_produto FOREIGN KEY (produto_id)
        REFERENCES produtos (id) ON DELETE CASCADE
);

CREATE TABLE carrinhos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_carrinhos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE itens_carrinhos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    carrinho_id UUID NOT NULL,
    variacao_produto_id UUID NOT NULL,
    quantidade INTEGER NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_itens_carrinhos_variacao UNIQUE (carrinho_id, variacao_produto_id),
    CONSTRAINT ck_itens_carrinhos_quantidade CHECK (quantidade > 0),
    CONSTRAINT fk_itens_carrinhos_carrinho FOREIGN KEY (carrinho_id)
        REFERENCES carrinhos (id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_carrinhos_variacao FOREIGN KEY (variacao_produto_id)
        REFERENCES variacoes_produtos (id) ON DELETE CASCADE
);

CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    numero VARCHAR(30) NOT NULL UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'AGUARDANDO_PAGAMENTO',
    subtotal NUMERIC(12, 2) NOT NULL,
    desconto NUMERIC(12, 2) NOT NULL DEFAULT 0,
    frete NUMERIC(12, 2) NOT NULL DEFAULT 0,
    valor_total NUMERIC(12, 2) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_pedidos_status CHECK (status IN (
        'AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_SEPARACAO',
        'ENVIADO', 'ENTREGUE', 'CANCELADO'
    )),
    CONSTRAINT ck_pedidos_subtotal CHECK (subtotal >= 0),
    CONSTRAINT ck_pedidos_desconto CHECK (desconto >= 0 AND desconto <= subtotal),
    CONSTRAINT ck_pedidos_frete CHECK (frete >= 0),
    CONSTRAINT ck_pedidos_valor_total CHECK (
        valor_total >= 0 AND valor_total = subtotal - desconto + frete
    ),
    -- Redundante com a PK, mas habilita a FK composta de avaliacoes (dono do pedido).
    CONSTRAINT uq_pedidos_id_usuario UNIQUE (id, usuario_id),
    CONSTRAINT fk_pedidos_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE TABLE itens_pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    variacao_produto_id UUID,
    sku VARCHAR(60) NOT NULL,
    nome_produto VARCHAR(180) NOT NULL,
    nome_variacao VARCHAR(120) NOT NULL,
    preco_unitario NUMERIC(12, 2) NOT NULL,
    quantidade INTEGER NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_itens_pedidos_preco CHECK (preco_unitario >= 0),
    CONSTRAINT ck_itens_pedidos_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_itens_pedidos_subtotal CHECK (
        subtotal = preco_unitario * quantidade
    ),
    CONSTRAINT fk_itens_pedidos_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE CASCADE,
    CONSTRAINT fk_itens_pedidos_variacao FOREIGN KEY (variacao_produto_id)
        REFERENCES variacoes_produtos (id) ON DELETE SET NULL
);

CREATE TABLE enderecos_pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL UNIQUE,
    destinatario VARCHAR(120) NOT NULL,
    cep CHAR(8) NOT NULL,
    rua VARCHAR(180) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(120),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_enderecos_pedidos_cep CHECK (cep ~ '^[0-9]{8}$'),
    CONSTRAINT ck_enderecos_pedidos_uf CHECK (uf ~ '^[A-Z]{2}$'),
    CONSTRAINT fk_enderecos_pedidos_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE CASCADE
);

CREATE TABLE pagamentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    metodo VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    valor NUMERIC(12, 2) NOT NULL,
    chave_idempotencia VARCHAR(255) NOT NULL UNIQUE,
    transacao_gateway_id VARCHAR(255) UNIQUE,
    pago_em TIMESTAMPTZ,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_pagamentos_metodo CHECK (metodo IN ('PIX', 'CARTAO', 'BOLETO')),
    CONSTRAINT ck_pagamentos_status CHECK (status IN (
        'PENDENTE', 'APROVADO', 'RECUSADO', 'CANCELADO', 'ESTORNADO'
    )),
    CONSTRAINT ck_pagamentos_valor CHECK (valor > 0),
    CONSTRAINT ck_pagamentos_pago_em CHECK (
        status <> 'APROVADO' OR pago_em IS NOT NULL
    ),
    CONSTRAINT fk_pagamentos_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE RESTRICT
);

CREATE TABLE entregas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL UNIQUE,
    transportadora VARCHAR(100),
    codigo_rastreio VARCHAR(120) UNIQUE,
    status VARCHAR(30) NOT NULL DEFAULT 'AGUARDANDO_ENVIO',
    enviado_em TIMESTAMPTZ,
    entregue_em TIMESTAMPTZ,
    previsao_entrega DATE,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_entregas_status CHECK (status IN (
        'AGUARDANDO_ENVIO', 'ENVIADO', 'EM_TRANSITO', 'ENTREGUE', 'DEVOLVIDO'
    )),
    CONSTRAINT ck_entregas_datas CHECK (
        entregue_em IS NULL OR enviado_em IS NOT NULL
    ),
    CONSTRAINT fk_entregas_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE RESTRICT
);

CREATE TABLE movimentacoes_estoque (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    variacao_produto_id UUID NOT NULL,
    pedido_id UUID,
    usuario_id UUID,
    tipo VARCHAR(30) NOT NULL,
    quantidade INTEGER NOT NULL,
    estoque_anterior INTEGER NOT NULL,
    estoque_novo INTEGER NOT NULL,
    observacao VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_movimentacoes_estoque_tipo CHECK (tipo IN (
        'ENTRADA', 'SAIDA', 'VENDA', 'CANCELAMENTO'
    )),
    CONSTRAINT ck_movimentacoes_estoque_quantidade CHECK (quantidade > 0),
    CONSTRAINT ck_movimentacoes_estoque_saldos CHECK (
        estoque_anterior >= 0 AND estoque_novo >= 0
    ),
    CONSTRAINT ck_movimentacoes_estoque_consistencia CHECK (
        (tipo IN ('ENTRADA', 'CANCELAMENTO')
            AND estoque_novo = estoque_anterior + quantidade)
        OR
        (tipo IN ('VENDA', 'SAIDA')
            AND estoque_novo = estoque_anterior - quantidade)
    ),
    CONSTRAINT ck_movimentacoes_estoque_pedido CHECK (
        (tipo IN ('VENDA', 'CANCELAMENTO') AND pedido_id IS NOT NULL)
        OR (tipo IN ('ENTRADA', 'SAIDA') AND pedido_id IS NULL)
    ),
    CONSTRAINT fk_movimentacoes_estoque_variacao FOREIGN KEY (variacao_produto_id)
        REFERENCES variacoes_produtos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_movimentacoes_estoque_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_movimentacoes_estoque_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

CREATE TABLE historicos_status_pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL,
    usuario_id UUID,
    status_anterior VARCHAR(30),
    status_novo VARCHAR(30) NOT NULL,
    motivo VARCHAR(255),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_historicos_status_anterior CHECK (
        status_anterior IS NULL OR status_anterior IN (
            'AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_SEPARACAO',
            'ENVIADO', 'ENTREGUE', 'CANCELADO'
        )
    ),
    CONSTRAINT ck_historicos_status_novo CHECK (status_novo IN (
        'AGUARDANDO_PAGAMENTO', 'PAGO', 'EM_SEPARACAO',
        'ENVIADO', 'ENTREGUE', 'CANCELADO'
    )),
    CONSTRAINT ck_historicos_status_alteracao CHECK (
        status_anterior IS NULL OR status_anterior <> status_novo
    ),
    CONSTRAINT fk_historicos_status_pedido FOREIGN KEY (pedido_id)
        REFERENCES pedidos (id) ON DELETE RESTRICT,
    CONSTRAINT fk_historicos_status_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT
);

-- Uma avaliação por cliente por produto; exige um pedido como prova de compra.
-- A FK composta (pedido_id, usuario_id) garante no banco que o pedido é do próprio avaliador.
CREATE TABLE avaliacoes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL,
    produto_id UUID NOT NULL,
    pedido_id UUID NOT NULL,
    nota SMALLINT NOT NULL,
    comentario TEXT,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_avaliacoes_usuario_produto UNIQUE (usuario_id, produto_id),
    CONSTRAINT ck_avaliacoes_nota CHECK (nota BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacoes_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacoes_produto FOREIGN KEY (produto_id)
        REFERENCES produtos (id) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacoes_pedido_do_usuario FOREIGN KEY (pedido_id, usuario_id)
        REFERENCES pedidos (id, usuario_id) ON DELETE CASCADE
);

-- Índices para foreign keys e filtros frequentes da API.
CREATE INDEX idx_usuarios_ativo ON usuarios (ativo);
CREATE UNIQUE INDEX uq_usuarios_email_normalizado
    ON usuarios (LOWER(BTRIM(email)));
CREATE INDEX idx_usuarios_perfis_usuario_id ON usuarios_perfis (usuario_id);
CREATE INDEX idx_usuarios_perfis_perfil_id ON usuarios_perfis (perfil_id);
CREATE INDEX idx_enderecos_usuario_id ON enderecos (usuario_id);
CREATE UNIQUE INDEX uq_enderecos_principal_por_usuario
    ON enderecos (usuario_id) WHERE principal = TRUE;

CREATE INDEX idx_categorias_ativo ON categorias (ativo);
CREATE INDEX idx_produtos_categoria_id ON produtos (categoria_id);
CREATE INDEX idx_produtos_ativo ON produtos (ativo);
CREATE INDEX idx_produtos_nome ON produtos (nome);
CREATE INDEX idx_variacoes_produtos_produto_id ON variacoes_produtos (produto_id);
CREATE INDEX idx_variacoes_produtos_ativo ON variacoes_produtos (ativo);
CREATE INDEX idx_imagens_produtos_produto_id ON imagens_produtos (produto_id);
CREATE UNIQUE INDEX uq_imagem_principal_por_produto
    ON imagens_produtos (produto_id) WHERE principal = TRUE;

CREATE INDEX idx_favoritos_usuario_id ON favoritos (usuario_id);
CREATE INDEX idx_favoritos_produto_id ON favoritos (produto_id);
CREATE INDEX idx_itens_carrinhos_carrinho_id ON itens_carrinhos (carrinho_id);
CREATE INDEX idx_itens_carrinhos_variacao_id ON itens_carrinhos (variacao_produto_id);

CREATE INDEX idx_pedidos_usuario_id ON pedidos (usuario_id);
CREATE INDEX idx_pedidos_status ON pedidos (status);
CREATE INDEX idx_pedidos_criado_em ON pedidos (criado_em);
CREATE INDEX idx_itens_pedidos_pedido_id ON itens_pedidos (pedido_id);
CREATE INDEX idx_itens_pedidos_variacao_id ON itens_pedidos (variacao_produto_id);
CREATE INDEX idx_pagamentos_pedido_id ON pagamentos (pedido_id);
CREATE INDEX idx_pagamentos_status ON pagamentos (status);
CREATE INDEX idx_entregas_status ON entregas (status);
CREATE INDEX idx_movimentacoes_estoque_variacao_id
    ON movimentacoes_estoque (variacao_produto_id);
CREATE INDEX idx_movimentacoes_estoque_pedido_id
    ON movimentacoes_estoque (pedido_id);
CREATE INDEX idx_movimentacoes_estoque_usuario_id
    ON movimentacoes_estoque (usuario_id);
CREATE INDEX idx_movimentacoes_estoque_criado_em
    ON movimentacoes_estoque (criado_em);
CREATE INDEX idx_historicos_status_pedido_id
    ON historicos_status_pedidos (pedido_id);
CREATE INDEX idx_historicos_status_criado_em
    ON historicos_status_pedidos (criado_em);

CREATE INDEX idx_avaliacoes_produto_id ON avaliacoes (produto_id);
CREATE INDEX idx_avaliacoes_usuario_id ON avaliacoes (usuario_id);

-- Dados obrigatórios de autorização. Em produção, mantenha este seed em uma migration Flyway.
INSERT INTO perfis (id, nome)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'CLIENTE'),
    ('00000000-0000-0000-0000-000000000002', 'ADMIN')
ON CONFLICT (nome) DO NOTHING;

-- Atualiza atualizado_em no banco, inclusive em alterações feitas fora da aplicação.
CREATE OR REPLACE FUNCTION definir_atualizado_em()
RETURNS TRIGGER AS $$
BEGIN
    NEW.atualizado_em = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Registros de auditoria são append-only: podem ser inseridos, nunca alterados ou removidos.
CREATE OR REPLACE FUNCTION impedir_alteracao_historico()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'A tabela % é imutável; UPDATE e DELETE não são permitidos.', TG_TABLE_NAME;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimentacoes_estoque_imutavel
BEFORE UPDATE OR DELETE ON movimentacoes_estoque
FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_historico();

CREATE TRIGGER trg_historicos_status_pedidos_imutavel
BEFORE UPDATE OR DELETE ON historicos_status_pedidos
FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_historico();

CREATE TRIGGER trg_usuarios_atualizado_em BEFORE UPDATE ON usuarios
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_perfis_atualizado_em BEFORE UPDATE ON perfis
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_usuarios_perfis_atualizado_em BEFORE UPDATE ON usuarios_perfis
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_enderecos_atualizado_em BEFORE UPDATE ON enderecos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_categorias_atualizado_em BEFORE UPDATE ON categorias
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_produtos_atualizado_em BEFORE UPDATE ON produtos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_variacoes_produtos_atualizado_em BEFORE UPDATE ON variacoes_produtos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_imagens_produtos_atualizado_em BEFORE UPDATE ON imagens_produtos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_favoritos_atualizado_em BEFORE UPDATE ON favoritos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_carrinhos_atualizado_em BEFORE UPDATE ON carrinhos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_itens_carrinhos_atualizado_em BEFORE UPDATE ON itens_carrinhos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_pedidos_atualizado_em BEFORE UPDATE ON pedidos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_itens_pedidos_atualizado_em BEFORE UPDATE ON itens_pedidos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_enderecos_pedidos_atualizado_em BEFORE UPDATE ON enderecos_pedidos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_pagamentos_atualizado_em BEFORE UPDATE ON pagamentos
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_entregas_atualizado_em BEFORE UPDATE ON entregas
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
CREATE TRIGGER trg_avaliacoes_atualizado_em BEFORE UPDATE ON avaliacoes
FOR EACH ROW EXECUTE FUNCTION definir_atualizado_em();
