-- Tokens JWT revogados no logout. O token continua com assinatura valida ate expirar,
-- entao o identificador dele (jti) fica nesta lista ate a data de expiracao; depois e apagado.
CREATE TABLE revoked_token (
    jti        VARCHAR(36)  PRIMARY KEY,
    user_id    BIGINT       NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    revoked_at TIMESTAMP(6) NOT NULL
);
CREATE INDEX idx_revoked_token_expires_at ON revoked_token (expires_at);
