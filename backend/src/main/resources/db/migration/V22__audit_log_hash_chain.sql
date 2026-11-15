-- Protecao dos registros de auditoria contra alteracao (encadeamento por hash).
-- Cada registro novo guarda o hash (SHA-256) do registro anterior e o proprio hash, calculado sobre o hash anterior
-- e o seu conteudo. Alterar, apagar ou inserir um registro no meio quebra a corrente, e a verificacao aponta onde.
-- Os registros anteriores a esta migracao ficam sem hash (nao fazem parte da corrente) e saem no expurgo de 6 meses.
ALTER TABLE audit_log ADD COLUMN previous_hash VARCHAR(64);
ALTER TABLE audit_log ADD COLUMN hash VARCHAR(64);

-- Ultimo hash da corrente (uma linha so). E travado a cada gravacao, para dois registros gravados ao mesmo tempo nao
-- usarem o mesmo hash anterior; tambem permite detectar quando o registro mais recente e apagado.
CREATE TABLE audit_chain_head (
    id        INTEGER PRIMARY KEY,
    last_hash VARCHAR(64)
);
INSERT INTO audit_chain_head (id, last_hash) VALUES (1, NULL);
