-- As 6 notas RIASEC do O*NET por carreira (V18) serviam so para o desempate pela correlacao de Pearson, que saiu da
-- recomendacao: agora o desempate usa o 2o e o 3o tipo do codigo de Holland. O codigo da ocupacao (onet_code) fica.
ALTER TABLE career DROP COLUMN onet_realistic;
ALTER TABLE career DROP COLUMN onet_investigative;
ALTER TABLE career DROP COLUMN onet_artistic;
ALTER TABLE career DROP COLUMN onet_social;
ALTER TABLE career DROP COLUMN onet_enterprising;
ALTER TABLE career DROP COLUMN onet_conventional;
