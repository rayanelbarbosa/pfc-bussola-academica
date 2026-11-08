-- Remove as "areas recomendadas" da primeira versao do teste (uniao das areas fixas de cada categoria RIASEC).
-- Desde a entrega de 09/11 a recomendacao e feita por carreira, dentro das areas de interesse escolhidas
-- (tabela career), e esse campo era gravado sem ser exibido em nenhuma tela.
ALTER TABLE vocational_test_result DROP COLUMN recommended_areas;
