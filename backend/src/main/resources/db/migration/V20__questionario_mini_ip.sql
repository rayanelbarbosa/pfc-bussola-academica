-- Questionario vocacional: O*NET Mini Interest Profiler (Mini-IP), versao de 30 itens validada pelo O*NET
-- (Rounds, Ming, Cao, Song e Lewis, 2016). 5 atividades de trabalho por tipo RIASEC, na ordem oficial
-- (blocos de 6: R, I, A, S, E, C). Traducao do projeto.
-- Fonte: O*NET Career Exploration Tools, USDOL/ETA (https://www.onetcenter.org/license_tools.html).
-- Modificado (traduzido) por Rayane da Luz Barbosa. USDOL/ETA has not approved, endorsed, or tested these
-- modifications.
-- Resposta de 0 a 4 (escala do O*NET); escore do tipo = soma das 5 respostas (0 a 20).
DELETE FROM question;

INSERT INTO question (id, statement, category, display_order) VALUES
    (1,  'Montar armários de cozinha', 'REALISTIC', 1),
    (2,  'Desenvolver um novo medicamento', 'INVESTIGATIVE', 2),
    (3,  'Escrever livros ou peças de teatro', 'ARTISTIC', 3),
    (4,  'Ajudar pessoas com problemas pessoais ou emocionais', 'SOCIAL', 4),
    (5,  'Gerenciar um departamento de uma grande empresa', 'ENTERPRISING', 5),
    (6,  'Instalar programas nos computadores de uma grande rede', 'CONVENTIONAL', 6),
    (7,  'Consertar eletrodomésticos', 'REALISTIC', 7),
    (8,  'Estudar formas de reduzir a poluição da água', 'INVESTIGATIVE', 8),
    (9,  'Compor ou fazer arranjos musicais', 'ARTISTIC', 9),
    (10, 'Dar orientação profissional a pessoas', 'SOCIAL', 10),
    (11, 'Abrir o seu próprio negócio', 'ENTERPRISING', 11),
    (12, 'Operar uma calculadora', 'CONVENTIONAL', 12),
    (13, 'Montar peças eletrônicas', 'REALISTIC', 13),
    (14, 'Fazer experimentos químicos', 'INVESTIGATIVE', 14),
    (15, 'Criar efeitos especiais para filmes', 'ARTISTIC', 15),
    (16, 'Fazer terapia de reabilitação com pacientes', 'SOCIAL', 16),
    (17, 'Negociar contratos comerciais', 'ENTERPRISING', 17),
    (18, 'Controlar os registros de envio e recebimento de mercadorias', 'CONVENTIONAL', 18),
    (19, 'Dirigir um caminhão para entregar encomendas em empresas e casas', 'REALISTIC', 19),
    (20, 'Examinar amostras de sangue no microscópio', 'INVESTIGATIVE', 20),
    (21, 'Pintar cenários para peças de teatro', 'ARTISTIC', 21),
    (22, 'Fazer trabalho voluntário em uma ONG', 'SOCIAL', 22),
    (23, 'Fazer o marketing de uma nova coleção de roupas', 'ENTERPRISING', 23),
    (24, 'Fazer o inventário de materiais com um coletor de dados portátil', 'CONVENTIONAL', 24),
    (25, 'Testar a qualidade de peças antes do envio', 'REALISTIC', 25),
    (26, 'Criar um jeito de prever melhor o tempo', 'INVESTIGATIVE', 26),
    (27, 'Escrever roteiros para filmes ou programas de TV', 'ARTISTIC', 27),
    (28, 'Dar aula para uma turma do ensino médio', 'SOCIAL', 28),
    (29, 'Vender produtos em uma loja de departamentos', 'ENTERPRISING', 29),
    (30, 'Carimbar, separar e distribuir a correspondência de uma empresa', 'CONVENTIONAL', 30);

-- Pontuacao maxima de cada tipo no resultado: os antigos (12 perguntas de 1 a 5) iam ate 10; os novos ate 20.
ALTER TABLE vocational_test_result ADD COLUMN max_score INTEGER NOT NULL DEFAULT 10;
