-- Cada resultado passa a pertencer a um usuario. Resultados antigos (anteriores ao login) ficam sem dono.
ALTER TABLE vocational_test_result ADD COLUMN user_id BIGINT;
ALTER TABLE vocational_test_result
    ADD CONSTRAINT fk_vocational_test_result_user FOREIGN KEY (user_id) REFERENCES app_user (id);
CREATE INDEX idx_vocational_test_result_user ON vocational_test_result (user_id);
