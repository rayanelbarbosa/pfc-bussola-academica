-- Questionario vocacional RIASEC: 12 perguntas fixas, 2 por categoria.
CREATE TABLE question (
    id            INTEGER      PRIMARY KEY,
    statement     VARCHAR(300) NOT NULL,
    category      VARCHAR(20)  NOT NULL,
    display_order INTEGER      NOT NULL UNIQUE,
    CONSTRAINT ck_question_category CHECK (category IN
        ('REALISTIC', 'INVESTIGATIVE', 'ARTISTIC', 'SOCIAL', 'ENTERPRISING', 'CONVENTIONAL'))
);

INSERT INTO question (id, statement, category, display_order) VALUES
    (1,  'Gosto de montar, consertar ou construir coisas com as mãos.', 'REALISTIC', 1),
    (2,  'Prefiro atividades práticas a atividades só teóricas.', 'REALISTIC', 2),
    (3,  'Gosto de investigar como as coisas funcionam e resolver problemas complexos.', 'INVESTIGATIVE', 3),
    (4,  'Tenho curiosidade científica e gosto de analisar dados e experimentos.', 'INVESTIGATIVE', 4),
    (5,  'Gosto de atividades criativas, como desenhar, escrever ou compor.', 'ARTISTIC', 5),
    (6,  'Prefiro ambientes com liberdade para me expressar de forma original.', 'ARTISTIC', 6),
    (7,  'Gosto de ajudar, ensinar ou cuidar de outras pessoas.', 'SOCIAL', 7),
    (8,  'Me sinto bem trabalhando em equipe e ouvindo os problemas dos outros.', 'SOCIAL', 8),
    (9,  'Gosto de liderar projetos e convencer pessoas sobre uma ideia.', 'ENTERPRISING', 9),
    (10, 'Tenho interesse em negócios, vendas ou empreender algo próprio.', 'ENTERPRISING', 10),
    (11, 'Gosto de organizar informações, planilhas e seguir procedimentos claros.', 'CONVENTIONAL', 11),
    (12, 'Prefiro tarefas com regras bem definidas a tarefas muito abertas.', 'CONVENTIONAL', 12);
