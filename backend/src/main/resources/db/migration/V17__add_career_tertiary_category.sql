-- Código de Holland com 3 letras para todas as carreiras, calculado a partir da base oficial do O*NET 31.0
-- (arquivo Career Interest Types, escala OI "Occupational Interests", fevereiro de 2026): para a ocupação
-- correspondente a cada curso, os três tipos RIASEC de maior nota, em ordem. Isso corrige os códigos de 2 letras
-- das migrations V7 e V16 (alguns tinham sido adaptados à mão) e acrescenta o 3º tipo, que desempata carreiras
-- com a mesma compatibilidade. Cada linha registra a ocupação do O*NET usada, para conferência.
-- O 3º tipo é opcional para carreiras novas cadastradas pelo administrador.
ALTER TABLE career ADD COLUMN tertiary_category VARCHAR(20);

-- Medicina: 29-1216.00 General Internal Medicine Physicians
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'SOCIAL', tertiary_category = 'REALISTIC' WHERE id = 1;
-- Enfermagem: 29-1141.00 Registered Nurses
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'CONVENTIONAL', tertiary_category = 'INVESTIGATIVE' WHERE id = 2;
-- Fisioterapia: 29-1123.00 Physical Therapists
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'REALISTIC' WHERE id = 3;
-- Odontologia: 29-1021.00 Dentists, General
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'SOCIAL' WHERE id = 4;
-- Nutrição: 29-1031.00 Dietitians and Nutritionists
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'ENTERPRISING' WHERE id = 5;
-- Farmácia: 29-1051.00 Pharmacists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'SOCIAL', tertiary_category = 'CONVENTIONAL' WHERE id = 6;
-- Biomedicina: 29-2011.00 Medical and Clinical Laboratory Technologists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 7;
-- Ciência da Computação: 15-1221.00 Computer and Information Research Scientists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'REALISTIC' WHERE id = 8;
-- Engenharia de Software: 15-1252.00 Software Developers
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'REALISTIC' WHERE id = 9;
-- Análise e Desenvolvimento de Sistemas: 15-1211.00 Computer Systems Analysts
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'REALISTIC' WHERE id = 10;
-- Sistemas de Informação: 11-3021.00 Computer and Information Systems Managers
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'ENTERPRISING', tertiary_category = 'INVESTIGATIVE' WHERE id = 11;
-- Ciência de Dados: 15-2051.00 Data Scientists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'ARTISTIC' WHERE id = 12;
-- Matemática: 15-2021.00 Mathematicians
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'ARTISTIC' WHERE id = 13;
-- Física: 19-2012.00 Physicists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 14;
-- Engenharia Civil: 17-2051.00 Civil Engineers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 15;
-- Engenharia Mecânica: 17-2141.00 Mechanical Engineers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 16;
-- Engenharia Elétrica: 17-2071.00 Electrical Engineers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 17;
-- Engenharia de Produção: 17-2112.00 Industrial Engineers
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'REALISTIC' WHERE id = 18;
-- Engenharia Química: 17-2041.00 Chemical Engineers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 19;
-- Engenharia Ambiental: 17-2081.00 Environmental Engineers
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 20;
-- Engenharia Mecatrônica: 17-2199.05 Mechatronics Engineers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 21;
-- Direito: 23-1011.00 Lawyers
UPDATE career SET primary_category = 'ENTERPRISING', secondary_category = 'CONVENTIONAL', tertiary_category = 'INVESTIGATIVE' WHERE id = 22;
-- Psicologia: 19-3033.00 Clinical and Counseling Psychologists
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 23;
-- Ciências Sociais: 19-3041.00 Sociologists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'SOCIAL', tertiary_category = 'ARTISTIC' WHERE id = 24;
-- Relações Internacionais: 19-3094.00 Political Scientists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'ARTISTIC', tertiary_category = 'ENTERPRISING' WHERE id = 25;
-- Serviço Social: 21-1021.00 Child, Family, and School Social Workers
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'CONVENTIONAL', tertiary_category = 'INVESTIGATIVE' WHERE id = 26;
-- História: 19-3093.00 Historians
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'ARTISTIC' WHERE id = 27;
-- Geografia: 19-3092.00 Geographers
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'ARTISTIC' WHERE id = 28;
-- Administração: 11-1021.00 General and Operations Managers
UPDATE career SET primary_category = 'ENTERPRISING', secondary_category = 'CONVENTIONAL', tertiary_category = 'SOCIAL' WHERE id = 29;
-- Ciências Contábeis: 13-2011.00 Accountants and Auditors
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'ENTERPRISING', tertiary_category = 'INVESTIGATIVE' WHERE id = 30;
-- Economia: 19-3011.00 Economists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'ENTERPRISING' WHERE id = 31;
-- Marketing: 11-2021.00 Marketing Managers
UPDATE career SET primary_category = 'ENTERPRISING', secondary_category = 'CONVENTIONAL', tertiary_category = 'INVESTIGATIVE' WHERE id = 32;
-- Gestão de Recursos Humanos: 11-3121.00 Human Resources Managers
UPDATE career SET primary_category = 'ENTERPRISING', secondary_category = 'CONVENTIONAL', tertiary_category = 'SOCIAL' WHERE id = 33;
-- Comércio Exterior: 13-1041.08 Customs Brokers
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'ENTERPRISING', tertiary_category = 'REALISTIC' WHERE id = 34;
-- Logística: 13-1081.00 Logisticians
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'ENTERPRISING', tertiary_category = 'INVESTIGATIVE' WHERE id = 35;
-- Design Gráfico: 27-1024.00 Graphic Designers
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'CONVENTIONAL', tertiary_category = 'ENTERPRISING' WHERE id = 36;
-- Arquitetura e Urbanismo: 17-1011.00 Architects, Except Landscape and Naval
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'CONVENTIONAL', tertiary_category = 'ARTISTIC' WHERE id = 37;
-- Publicidade e Propaganda: 11-2011.00 Advertising and Promotions Managers
UPDATE career SET primary_category = 'ENTERPRISING', secondary_category = 'CONVENTIONAL', tertiary_category = 'ARTISTIC' WHERE id = 38;
-- Jornalismo: 27-3023.00 News Analysts, Reporters, and Journalists
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'ENTERPRISING' WHERE id = 39;
-- Cinema e Audiovisual: 27-2012.00 Producers and Directors
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'ENTERPRISING', tertiary_category = 'CONVENTIONAL' WHERE id = 40;
-- Design de Moda: 27-1022.00 Fashion Designers
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'REALISTIC', tertiary_category = 'ENTERPRISING' WHERE id = 41;
-- Música: 27-2042.00 Musicians and Singers
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'ENTERPRISING', tertiary_category = 'SOCIAL' WHERE id = 42;
-- Pedagogia: 25-2021.00 Elementary School Teachers, Except Special Education
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'ARTISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 43;
-- Letras: 25-1123.00 English Language and Literature Teachers, Postsecondary
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'ARTISTIC' WHERE id = 44;
-- Licenciatura em Matemática: 25-1022.00 Mathematical Science Teachers, Postsecondary
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 45;
-- Educação Física: 39-9031.00 Exercise Trainers and Group Fitness Instructors
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'REALISTIC', tertiary_category = 'ENTERPRISING' WHERE id = 46;
-- Licenciatura em Ciências Biológicas: 25-1042.00 Biological Science Teachers, Postsecondary
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'REALISTIC' WHERE id = 47;
-- Licenciatura em História: 25-1125.00 History Teachers, Postsecondary
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'ARTISTIC' WHERE id = 48;
-- Psicopedagogia: 21-1012.00 Educational, Guidance, and Career Counselors and Advisors
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'CONVENTIONAL', tertiary_category = 'ENTERPRISING' WHERE id = 49;
-- Medicina Veterinária: 29-1131.00 Veterinarians
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 1001;
-- Fonoaudiologia: 29-1127.00 Speech-Language Pathologists
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 1002;
-- Terapia Ocupacional: 29-1122.00 Occupational Therapists
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'REALISTIC' WHERE id = 1003;
-- Química: 19-2031.00 Chemists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 1004;
-- Estatística: 15-2041.00 Statisticians
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'CONVENTIONAL', tertiary_category = 'REALISTIC' WHERE id = 1005;
-- Segurança da Informação: 15-1212.00 Information Security Analysts
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'REALISTIC' WHERE id = 1006;
-- Engenharia de Computação: 17-2061.00 Computer Hardware Engineers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 1007;
-- Engenharia Aeronáutica: 17-2011.00 Aerospace Engineers
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 1008;
-- Agronomia: 19-1013.00 Soil and Plant Scientists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'REALISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 1009;
-- Antropologia: 19-3091.00 Anthropologists and Archeologists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'ARTISTIC', tertiary_category = 'REALISTIC' WHERE id = 1010;
-- Ciência Política: 19-3094.00 Political Scientists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'ARTISTIC', tertiary_category = 'ENTERPRISING' WHERE id = 1011;
-- Arqueologia: 19-3091.00 Anthropologists and Archeologists
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'ARTISTIC', tertiary_category = 'REALISTIC' WHERE id = 1012;
-- Finanças: 13-2051.00 Financial and Investment Analysts
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'ENTERPRISING', tertiary_category = 'INVESTIGATIVE' WHERE id = 1013;
-- Turismo: 41-3041.00 Travel Agents
UPDATE career SET primary_category = 'ENTERPRISING', secondary_category = 'CONVENTIONAL', tertiary_category = 'SOCIAL' WHERE id = 1014;
-- Secretariado Executivo: 43-6011.00 Executive Secretaries and Executive Administrative Assistants
UPDATE career SET primary_category = 'CONVENTIONAL', secondary_category = 'ENTERPRISING', tertiary_category = 'SOCIAL' WHERE id = 1015;
-- Fotografia: 27-4021.00 Photographers
UPDATE career SET primary_category = 'REALISTIC', secondary_category = 'ARTISTIC', tertiary_category = 'CONVENTIONAL' WHERE id = 1016;
-- Artes Cênicas: 27-2011.00 Actors
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'SOCIAL', tertiary_category = 'ENTERPRISING' WHERE id = 1017;
-- Jogos Digitais: 15-1255.01 Video Game Designers
UPDATE career SET primary_category = 'ARTISTIC', secondary_category = 'INVESTIGATIVE', tertiary_category = 'ENTERPRISING' WHERE id = 1018;
-- Educação Especial: 25-2055.00 Special Education Teachers, Kindergarten
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'INVESTIGATIVE', tertiary_category = 'CONVENTIONAL' WHERE id = 1019;
-- Licenciatura em Química: 25-1052.00 Chemistry Teachers, Postsecondary
UPDATE career SET primary_category = 'INVESTIGATIVE', secondary_category = 'SOCIAL', tertiary_category = 'REALISTIC' WHERE id = 1020;
-- Licenciatura em Artes Visuais: 25-1121.00 Art, Drama, and Music Teachers, Postsecondary
UPDATE career SET primary_category = 'SOCIAL', secondary_category = 'ARTISTIC', tertiary_category = 'INVESTIGATIVE' WHERE id = 1021;
