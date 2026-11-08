-- O link oficial passa a ser opcional: nem toda forma de ingresso tem um site oficial unico
-- (ex.: faculdades particulares e portais de bolsas variam por instituicao).
-- Quando informado, continua precisando comecar com http:// ou https:// (validado na API).
ALTER TABLE admission_method ALTER COLUMN official_url DROP NOT NULL;
