-- Amplia o catálogo de 49 para 70 carreiras (3 novas por área de interesse).
-- Os códigos RIASEC daqui são provisórios: a V17 grava o código de 3 letras de todas as carreiras a partir
-- da base oficial do O*NET 31.0.
-- Os ids começam em 1001 de propósito: a sequence career_seq (V15) entrega 50, 51, ... para as carreiras
-- cadastradas pela tela, então a faixa 1001+ fica reservada para cargas feitas por migration e as duas
-- nunca colidem, mesmo que alguma carreira já tenha sido cadastrada pelo admin antes desta migration.
INSERT INTO career (id, name, area, primary_category, secondary_category, description) VALUES
(1001, 'Medicina Veterinária', 'HEALTH', 'INVESTIGATIVE', 'REALISTIC', 'Cuida da saúde dos animais, de pets a criações, e atua na segurança dos alimentos e na saúde pública.'),
(1002, 'Fonoaudiologia', 'HEALTH', 'SOCIAL', 'INVESTIGATIVE', 'Avalia e trata a comunicação humana: fala, voz, audição, linguagem e deglutição.'),
(1003, 'Terapia Ocupacional', 'HEALTH', 'SOCIAL', 'REALISTIC', 'Ajuda pessoas a recuperar autonomia nas atividades do dia a dia após doenças, acidentes ou deficiências.'),
(1004, 'Química', 'EXACT_AND_TECHNOLOGY', 'INVESTIGATIVE', 'REALISTIC', 'Estuda a composição das substâncias e desenvolve materiais, medicamentos e processos em laboratório e na indústria.'),
(1005, 'Estatística', 'EXACT_AND_TECHNOLOGY', 'CONVENTIONAL', 'INVESTIGATIVE', 'Coleta, organiza e analisa dados para apoiar decisões em pesquisa, saúde, governo e empresas.'),
(1006, 'Segurança da Informação', 'EXACT_AND_TECHNOLOGY', 'INVESTIGATIVE', 'CONVENTIONAL', 'Protege sistemas e dados contra ataques, investigando falhas e definindo regras de acesso.'),
(1007, 'Engenharia de Computação', 'ENGINEERING', 'INVESTIGATIVE', 'REALISTIC', 'Une eletrônica e programação para projetar computadores, sistemas embarcados e dispositivos inteligentes.'),
(1008, 'Engenharia Aeronáutica', 'ENGINEERING', 'INVESTIGATIVE', 'REALISTIC', 'Projeta, testa e mantém aeronaves, motores e sistemas de voo.'),
(1009, 'Agronomia', 'ENGINEERING', 'REALISTIC', 'INVESTIGATIVE', 'Planeja a produção agrícola com tecnologia, cuidando do solo, das plantas e do uso sustentável dos recursos.'),
(1010, 'Antropologia', 'HUMANITIES', 'INVESTIGATIVE', 'ARTISTIC', 'Estuda culturas, costumes e modos de vida de diferentes grupos humanos, com pesquisa de campo.'),
(1011, 'Ciência Política', 'HUMANITIES', 'INVESTIGATIVE', 'ENTERPRISING', 'Analisa o poder, o governo, as eleições e as políticas públicas.'),
(1012, 'Arqueologia', 'HUMANITIES', 'INVESTIGATIVE', 'REALISTIC', 'Investiga o passado humano por meio de escavações e da análise de objetos e vestígios.'),
(1013, 'Finanças', 'BUSINESS', 'ENTERPRISING', 'CONVENTIONAL', 'Planeja investimentos, analisa riscos e cuida do dinheiro de empresas e pessoas.'),
(1014, 'Turismo', 'BUSINESS', 'ENTERPRISING', 'SOCIAL', 'Planeja viagens, eventos e roteiros, e gerencia serviços de hotelaria e lazer.'),
(1015, 'Secretariado Executivo', 'BUSINESS', 'CONVENTIONAL', 'ENTERPRISING', 'Organiza agendas, documentos e a comunicação da diretoria, apoiando a gestão da empresa.'),
(1016, 'Fotografia', 'ARTS_AND_COMMUNICATION', 'ARTISTIC', 'REALISTIC', 'Cria imagens para publicidade, jornalismo, moda e eventos, dominando luz, câmera e edição.'),
(1017, 'Artes Cênicas', 'ARTS_AND_COMMUNICATION', 'ARTISTIC', 'ENTERPRISING', 'Atua, dirige e produz espetáculos de teatro, cinema e televisão.'),
(1018, 'Jogos Digitais', 'ARTS_AND_COMMUNICATION', 'ARTISTIC', 'INVESTIGATIVE', 'Cria jogos para computador, console e celular, do roteiro e da arte à programação.'),
(1019, 'Educação Especial', 'EDUCATION', 'SOCIAL', 'ARTISTIC', 'Ensina e acompanha estudantes com deficiência, adaptando materiais e estratégias de aprendizagem.'),
(1020, 'Licenciatura em Química', 'EDUCATION', 'SOCIAL', 'INVESTIGATIVE', 'Forma professores de Química para o ensino fundamental e médio, com aulas práticas e experimentos.'),
(1021, 'Licenciatura em Artes Visuais', 'EDUCATION', 'SOCIAL', 'ARTISTIC', 'Forma professores de Arte, ensinando desenho, pintura, escultura e história da arte nas escolas.');
