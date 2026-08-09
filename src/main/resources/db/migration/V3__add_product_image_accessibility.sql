-- Política de acessibilidade para imagens de produtos.
--
-- Imagem informativa: decorativa = FALSE e texto_alternativo não vazio.
-- Imagem decorativa: decorativa = TRUE e texto_alternativo deve ser ''.
--
-- A constraint é NOT VALID para não bloquear a aplicação da migration por
-- registros legados que ainda precisam de saneamento. Ela continua sendo
-- aplicada a novos INSERTs e a UPDATEs das linhas existentes.

ALTER TABLE imagens_produtos
    ADD COLUMN IF NOT EXISTS decorativa BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN imagens_produtos.texto_alternativo IS
    'Texto alternativo da imagem; vazio somente quando decorativa = TRUE.';

COMMENT ON COLUMN imagens_produtos.decorativa IS
    'Indica imagem sem conteúdo informativo; deve usar texto_alternativo = ''.''';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conrelid = 'imagens_produtos'::regclass
          AND conname = 'ck_imagens_produtos_acessibilidade'
    ) THEN
        ALTER TABLE imagens_produtos
            ADD CONSTRAINT ck_imagens_produtos_acessibilidade
            CHECK (
                (
                    decorativa = TRUE
                    AND texto_alternativo = ''
                )
                OR
                (
                    decorativa = FALSE
                    AND texto_alternativo IS NOT NULL
                    AND texto_alternativo = btrim(texto_alternativo)
                    AND char_length(texto_alternativo) > 0
                )
            ) NOT VALID;
    END IF;
END
$$;
