-- Cadastro de carreiras pelo administrador: as carreiras deixam de ser fixas na migration V7.
-- id passa a vir de uma sequence (as 49 carreiras iniciais usam os ids 1 a 49).
-- Não há exclusão física: a carreira é desativada e some da recomendação e da busca de vídeos.
CREATE SEQUENCE career_seq START WITH 50 INCREMENT BY 1;

ALTER TABLE career ADD COLUMN active BOOLEAN DEFAULT TRUE NOT NULL;
ALTER TABLE career ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL;

CREATE INDEX idx_career_active ON career (active);
