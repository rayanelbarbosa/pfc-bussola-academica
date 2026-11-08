-- Acelera a contagem de tentativas de login recusadas por e-mail (bloqueio apos 5 falhas em 15 min).
CREATE INDEX idx_audit_log_email_action_created_at ON audit_log (user_email, action, created_at);
