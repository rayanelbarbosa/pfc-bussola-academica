-- Areas de interesse escolhidas antes do teste (de 1 a 3), ex.: "HEALTH;ENGINEERING".
-- Resultados antigos ficam sem areas e sao tratados como "Ainda nao sei".
ALTER TABLE vocational_test_result ADD COLUMN interest_areas VARCHAR(200);
