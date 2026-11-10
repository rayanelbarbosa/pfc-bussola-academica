-- Perfil de interesses completo de cada carreira: as 6 notas RIASEC (1 a 7) da ocupação correspondente na base
-- oficial do O*NET 31.0 (Career Interest Types, escala OI, fevereiro de 2026), as mesmas usadas na V17.
-- Uso: desempate da recomendação. Carreiras com a mesma compatibilidade (2 × principal + secundário) são ordenadas
-- pela correlação de Pearson entre os 6 escores do estudante e as 6 notas da ocupação: o perfil inteiro, não só
-- 3 letras. Simulando todos os perfis de resposta possíveis, o código de 3 letras deixava 4 cursos sem nunca
-- aparecer (empates resolvidos pela ordem alfabética); com o perfil completo, todo curso aparece em pelo menos 7%.
-- Colunas opcionais: carreiras cadastradas pelo administrador sem perfil desempatam pelo 3º tipo do código.
ALTER TABLE career ADD COLUMN onet_code VARCHAR(10);
ALTER TABLE career ADD COLUMN onet_realistic DOUBLE PRECISION;
ALTER TABLE career ADD COLUMN onet_investigative DOUBLE PRECISION;
ALTER TABLE career ADD COLUMN onet_artistic DOUBLE PRECISION;
ALTER TABLE career ADD COLUMN onet_social DOUBLE PRECISION;
ALTER TABLE career ADD COLUMN onet_enterprising DOUBLE PRECISION;
ALTER TABLE career ADD COLUMN onet_conventional DOUBLE PRECISION;

-- Medicina: 29-1216.00 General Internal Medicine Physicians
UPDATE career SET onet_code = '29-1216.00', onet_realistic = 4.43, onet_investigative = 6.81, onet_artistic = 1.44, onet_social = 6.08, onet_enterprising = 2.1, onet_conventional = 3.63 WHERE id = 1;
-- Enfermagem: 29-1141.00 Registered Nurses
UPDATE career SET onet_code = '29-1141.00', onet_realistic = 3.46, onet_investigative = 4.71, onet_artistic = 1.34, onet_social = 5.57, onet_enterprising = 2.38, onet_conventional = 4.75 WHERE id = 2;
-- Fisioterapia: 29-1123.00 Physical Therapists
UPDATE career SET onet_code = '29-1123.00', onet_realistic = 4.33, onet_investigative = 4.87, onet_artistic = 2.17, onet_social = 5.91, onet_enterprising = 2.49, onet_conventional = 3.07 WHERE id = 3;
-- Odontologia: 29-1021.00 Dentists, General
UPDATE career SET onet_code = '29-1021.00', onet_realistic = 5.23, onet_investigative = 5.48, onet_artistic = 1.17, onet_social = 4.41, onet_enterprising = 2, onet_conventional = 3.71 WHERE id = 4;
-- Nutrição: 29-1031.00 Dietitians and Nutritionists
UPDATE career SET onet_code = '29-1031.00', onet_realistic = 3.03, onet_investigative = 4.8, onet_artistic = 2.4, onet_social = 5.5, onet_enterprising = 3.42, onet_conventional = 3.35 WHERE id = 5;
-- Farmácia: 29-1051.00 Pharmacists
UPDATE career SET onet_code = '29-1051.00', onet_realistic = 2.82, onet_investigative = 5.67, onet_artistic = 1.48, onet_social = 4.88, onet_enterprising = 2.62, onet_conventional = 4.56 WHERE id = 6;
-- Biomedicina: 29-2011.00 Medical and Clinical Laboratory Technologists
UPDATE career SET onet_code = '29-2011.00', onet_realistic = 5.81, onet_investigative = 6.15, onet_artistic = 1.13, onet_social = 2.77, onet_enterprising = 1.51, onet_conventional = 4.99 WHERE id = 7;
-- Ciência da Computação: 15-1221.00 Computer and Information Research Scientists
UPDATE career SET onet_code = '15-1221.00', onet_realistic = 3.74, onet_investigative = 7, onet_artistic = 2.95, onet_social = 2, onet_enterprising = 2.49, onet_conventional = 4.8 WHERE id = 8;
-- Engenharia de Software: 15-1252.00 Software Developers
UPDATE career SET onet_code = '15-1252.00', onet_realistic = 3.61, onet_investigative = 6.05, onet_artistic = 2.37, onet_social = 1.81, onet_enterprising = 1.87, onet_conventional = 5.62 WHERE id = 9;
-- Análise e Desenvolvimento de Sistemas: 15-1211.00 Computer Systems Analysts
UPDATE career SET onet_code = '15-1211.00', onet_realistic = 3.27, onet_investigative = 6.39, onet_artistic = 1.98, onet_social = 1.91, onet_enterprising = 2.23, onet_conventional = 5.93 WHERE id = 10;
-- Sistemas de Informação: 11-3021.00 Computer and Information Systems Managers
UPDATE career SET onet_code = '11-3021.00', onet_realistic = 2.68, onet_investigative = 4.49, onet_artistic = 1.47, onet_social = 2.35, onet_enterprising = 5.09, onet_conventional = 5.94 WHERE id = 11;
-- Ciência de Dados: 15-2051.00 Data Scientists
UPDATE career SET onet_code = '15-2051.00', onet_realistic = 2.17, onet_investigative = 6.98, onet_artistic = 2.61, onet_social = 1.66, onet_enterprising = 1.71, onet_conventional = 5.39 WHERE id = 12;
-- Matemática: 15-2021.00 Mathematicians
UPDATE career SET onet_code = '15-2021.00', onet_realistic = 3.22, onet_investigative = 7, onet_artistic = 3.23, onet_social = 1.95, onet_enterprising = 1.11, onet_conventional = 5.03 WHERE id = 13;
-- Física: 19-2012.00 Physicists
UPDATE career SET onet_code = '19-2012.00', onet_realistic = 4.94, onet_investigative = 7, onet_artistic = 2.75, onet_social = 2.12, onet_enterprising = 1.77, onet_conventional = 4.8 WHERE id = 14;
-- Engenharia Civil: 17-2051.00 Civil Engineers
UPDATE career SET onet_code = '17-2051.00', onet_realistic = 6.43, onet_investigative = 5.15, onet_artistic = 2.21, onet_social = 1.69, onet_enterprising = 2.66, onet_conventional = 4.61 WHERE id = 15;
-- Engenharia Mecânica: 17-2141.00 Mechanical Engineers
UPDATE career SET onet_code = '17-2141.00', onet_realistic = 6.39, onet_investigative = 5.12, onet_artistic = 2.02, onet_social = 1.22, onet_enterprising = 1.97, onet_conventional = 4.83 WHERE id = 16;
-- Engenharia Elétrica: 17-2071.00 Electrical Engineers
UPDATE career SET onet_code = '17-2071.00', onet_realistic = 6.02, onet_investigative = 5.45, onet_artistic = 2.12, onet_social = 1.72, onet_enterprising = 2.44, onet_conventional = 4.43 WHERE id = 17;
-- Engenharia de Produção: 17-2112.00 Industrial Engineers
UPDATE career SET onet_code = '17-2112.00', onet_realistic = 4.96, onet_investigative = 4.99, onet_artistic = 1.58, onet_social = 1.37, onet_enterprising = 3.29, onet_conventional = 5.35 WHERE id = 18;
-- Engenharia Química: 17-2041.00 Chemical Engineers
UPDATE career SET onet_code = '17-2041.00', onet_realistic = 6.27, onet_investigative = 5.75, onet_artistic = 1.8, onet_social = 1.02, onet_enterprising = 1.85, onet_conventional = 4.54 WHERE id = 19;
-- Engenharia Ambiental: 17-2081.00 Environmental Engineers
UPDATE career SET onet_code = '17-2081.00', onet_realistic = 5.17, onet_investigative = 6.1, onet_artistic = 1.96, onet_social = 2.22, onet_enterprising = 3.17, onet_conventional = 4.59 WHERE id = 20;
-- Engenharia Mecatrônica: 17-2199.05 Mechatronics Engineers
UPDATE career SET onet_code = '17-2199.05', onet_realistic = 5.68, onet_investigative = 5.55, onet_artistic = 2.43, onet_social = 1.58, onet_enterprising = 1.72, onet_conventional = 4.84 WHERE id = 21;
-- Direito: 23-1011.00 Lawyers
UPDATE career SET onet_code = '23-1011.00', onet_realistic = 1.12, onet_investigative = 4.38, onet_artistic = 2.4, onet_social = 3.48, onet_enterprising = 5.51, onet_conventional = 4.59 WHERE id = 22;
-- Psicologia: 19-3033.00 Clinical and Counseling Psychologists
UPDATE career SET onet_code = '19-3033.00', onet_realistic = 1.05, onet_investigative = 5.58, onet_artistic = 3.06, onet_social = 6.57, onet_enterprising = 2.65, onet_conventional = 3.49 WHERE id = 23;
-- Ciências Sociais: 19-3041.00 Sociologists
UPDATE career SET onet_code = '19-3041.00', onet_realistic = 1.56, onet_investigative = 6.5, onet_artistic = 3.99, onet_social = 4.36, onet_enterprising = 3.08, onet_conventional = 3.6 WHERE id = 24;
-- Relações Internacionais: 19-3094.00 Political Scientists
UPDATE career SET onet_code = '19-3094.00', onet_realistic = 1, onet_investigative = 6.71, onet_artistic = 3.87, onet_social = 3.42, onet_enterprising = 3.61, onet_conventional = 3.52 WHERE id = 25;
-- Serviço Social: 21-1021.00 Child, Family, and School Social Workers
UPDATE career SET onet_code = '21-1021.00', onet_realistic = 1.7, onet_investigative = 3.32, onet_artistic = 2.9, onet_social = 6.85, onet_enterprising = 2.94, onet_conventional = 3.57 WHERE id = 26;
-- História: 19-3093.00 Historians
UPDATE career SET onet_code = '19-3093.00', onet_realistic = 2.39, onet_investigative = 5.32, onet_artistic = 3.7, onet_social = 3.22, onet_enterprising = 2.35, onet_conventional = 4.87 WHERE id = 27;
-- Geografia: 19-3092.00 Geographers
UPDATE career SET onet_code = '19-3092.00', onet_realistic = 5.11, onet_investigative = 6.74, onet_artistic = 3.69, onet_social = 2.72, onet_enterprising = 2.1, onet_conventional = 3.69 WHERE id = 28;
-- Administração: 11-1021.00 General and Operations Managers
UPDATE career SET onet_code = '11-1021.00', onet_realistic = 2.2, onet_investigative = 2.37, onet_artistic = 1.29, onet_social = 3.38, onet_enterprising = 7, onet_conventional = 5.34 WHERE id = 29;
-- Ciências Contábeis: 13-2011.00 Accountants and Auditors
UPDATE career SET onet_code = '13-2011.00', onet_realistic = 1.14, onet_investigative = 3.57, onet_artistic = 1.08, onet_social = 2.13, onet_enterprising = 3.87, onet_conventional = 7 WHERE id = 30;
-- Economia: 19-3011.00 Economists
UPDATE career SET onet_code = '19-3011.00', onet_realistic = 1.35, onet_investigative = 7, onet_artistic = 2.43, onet_social = 2.4, onet_enterprising = 3.93, onet_conventional = 4.53 WHERE id = 31;
-- Marketing: 11-2021.00 Marketing Managers
UPDATE career SET onet_code = '11-2021.00', onet_realistic = 1, onet_investigative = 2.9, onet_artistic = 2.45, onet_social = 2.82, onet_enterprising = 7, onet_conventional = 4.7 WHERE id = 32;
-- Gestão de Recursos Humanos: 11-3121.00 Human Resources Managers
UPDATE career SET onet_code = '11-3121.00', onet_realistic = 1.26, onet_investigative = 2.31, onet_artistic = 1.5, onet_social = 3.88, onet_enterprising = 7, onet_conventional = 5.54 WHERE id = 33;
-- Comércio Exterior: 13-1041.08 Customs Brokers
UPDATE career SET onet_code = '13-1041.08', onet_realistic = 2.37, onet_investigative = 1.53, onet_artistic = 1, onet_social = 2.17, onet_enterprising = 5.02, onet_conventional = 6.13 WHERE id = 34;
-- Logística: 13-1081.00 Logisticians
UPDATE career SET onet_code = '13-1081.00', onet_realistic = 2.35, onet_investigative = 3.81, onet_artistic = 1.1, onet_social = 2.33, onet_enterprising = 5.02, onet_conventional = 5.44 WHERE id = 35;
-- Design Gráfico: 27-1024.00 Graphic Designers
UPDATE career SET onet_code = '27-1024.00', onet_realistic = 3.38, onet_investigative = 2.86, onet_artistic = 7, onet_social = 2.16, onet_enterprising = 3.43, onet_conventional = 3.92 WHERE id = 36;
-- Arquitetura e Urbanismo: 17-1011.00 Architects, Except Landscape and Naval
UPDATE career SET onet_code = '17-1011.00', onet_realistic = 4.92, onet_investigative = 3.88, onet_artistic = 4.13, onet_social = 2.53, onet_enterprising = 3.65, onet_conventional = 4.44 WHERE id = 37;
-- Publicidade e Propaganda: 11-2011.00 Advertising and Promotions Managers
UPDATE career SET onet_code = '11-2011.00', onet_realistic = 1, onet_investigative = 1.69, onet_artistic = 3.87, onet_social = 3.14, onet_enterprising = 7, onet_conventional = 4.43 WHERE id = 38;
-- Jornalismo: 27-3023.00 News Analysts, Reporters, and Journalists
UPDATE career SET onet_code = '27-3023.00', onet_realistic = 1.47, onet_investigative = 4.16, onet_artistic = 5.09, onet_social = 2.8, onet_enterprising = 3.95, onet_conventional = 3.64 WHERE id = 39;
-- Cinema e Audiovisual: 27-2012.00 Producers and Directors
UPDATE career SET onet_code = '27-2012.00', onet_realistic = 2.13, onet_investigative = 2.24, onet_artistic = 6.14, onet_social = 3.12, onet_enterprising = 5.78, onet_conventional = 3.56 WHERE id = 40;
-- Design de Moda: 27-1022.00 Fashion Designers
UPDATE career SET onet_code = '27-1022.00', onet_realistic = 4.34, onet_investigative = 2.17, onet_artistic = 7, onet_social = 2.35, onet_enterprising = 3.96, onet_conventional = 2.94 WHERE id = 41;
-- Música: 27-2042.00 Musicians and Singers
UPDATE career SET onet_code = '27-2042.00', onet_realistic = 2.87, onet_investigative = 1.53, onet_artistic = 6.66, onet_social = 3.41, onet_enterprising = 3.42, onet_conventional = 2.17 WHERE id = 42;
-- Pedagogia: 25-2021.00 Elementary School Teachers, Except Special Education
UPDATE career SET onet_code = '25-2021.00', onet_realistic = 2.64, onet_investigative = 3.35, onet_artistic = 3.87, onet_social = 7, onet_enterprising = 2.57, onet_conventional = 3.78 WHERE id = 43;
-- Letras: 25-1123.00 English Language and Literature Teachers, Postsecondary
UPDATE career SET onet_code = '25-1123.00', onet_realistic = 2.28, onet_investigative = 4.78, onet_artistic = 4.19, onet_social = 7, onet_enterprising = 2.43, onet_conventional = 3.49 WHERE id = 44;
-- Licenciatura em Matemática: 25-1022.00 Mathematical Science Teachers, Postsecondary
UPDATE career SET onet_code = '25-1022.00', onet_realistic = 2.81, onet_investigative = 5.86, onet_artistic = 3.2, onet_social = 6.19, onet_enterprising = 1.94, onet_conventional = 4.38 WHERE id = 45;
-- Educação Física: 39-9031.00 Exercise Trainers and Group Fitness Instructors
UPDATE career SET onet_code = '39-9031.00', onet_realistic = 5.23, onet_investigative = 2.71, onet_artistic = 2.4, onet_social = 5.62, onet_enterprising = 3.51, onet_conventional = 2.98 WHERE id = 46;
-- Licenciatura em Ciências Biológicas: 25-1042.00 Biological Science Teachers, Postsecondary
UPDATE career SET onet_code = '25-1042.00', onet_realistic = 3.96, onet_investigative = 5.88, onet_artistic = 3.3, onet_social = 6.25, onet_enterprising = 2.04, onet_conventional = 3.65 WHERE id = 47;
-- Licenciatura em História: 25-1125.00 History Teachers, Postsecondary
UPDATE career SET onet_code = '25-1125.00', onet_realistic = 2.81, onet_investigative = 5.41, onet_artistic = 3.95, onet_social = 6.95, onet_enterprising = 2.6, onet_conventional = 3.45 WHERE id = 48;
-- Psicopedagogia: 21-1012.00 Educational, Guidance, and Career Counselors and Advisors
UPDATE career SET onet_code = '21-1012.00', onet_realistic = 1, onet_investigative = 3.3, onet_artistic = 3.13, onet_social = 7, onet_enterprising = 3.92, onet_conventional = 4.09 WHERE id = 49;
-- Medicina Veterinária: 29-1131.00 Veterinarians
UPDATE career SET onet_code = '29-1131.00', onet_realistic = 5.98, onet_investigative = 5.98, onet_artistic = 1, onet_social = 3.45, onet_enterprising = 1.7, onet_conventional = 3.47 WHERE id = 1001;
-- Fonoaudiologia: 29-1127.00 Speech-Language Pathologists
UPDATE career SET onet_code = '29-1127.00', onet_realistic = 2.62, onet_investigative = 5.57, onet_artistic = 3, onet_social = 6.09, onet_enterprising = 1.44, onet_conventional = 4.03 WHERE id = 1002;
-- Terapia Ocupacional: 29-1122.00 Occupational Therapists
UPDATE career SET onet_code = '29-1122.00', onet_realistic = 3.57, onet_investigative = 4.23, onet_artistic = 3.07, onet_social = 6.08, onet_enterprising = 1.83, onet_conventional = 3.12 WHERE id = 1003;
-- Química: 19-2031.00 Chemists
UPDATE career SET onet_code = '19-2031.00', onet_realistic = 5.67, onet_investigative = 6.71, onet_artistic = 1.52, onet_social = 1.14, onet_enterprising = 1.6, onet_conventional = 4.81 WHERE id = 1004;
-- Estatística: 15-2041.00 Statisticians
UPDATE career SET onet_code = '15-2041.00', onet_realistic = 2.14, onet_investigative = 6.58, onet_artistic = 1.77, onet_social = 1.71, onet_enterprising = 1.82, onet_conventional = 6.03 WHERE id = 1005;
-- Segurança da Informação: 15-1212.00 Information Security Analysts
UPDATE career SET onet_code = '15-1212.00', onet_realistic = 3.56, onet_investigative = 5.4, onet_artistic = 1.34, onet_social = 2.11, onet_enterprising = 2.85, onet_conventional = 6.08 WHERE id = 1006;
-- Engenharia de Computação: 17-2061.00 Computer Hardware Engineers
UPDATE career SET onet_code = '17-2061.00', onet_realistic = 5.69, onet_investigative = 5.56, onet_artistic = 1.83, onet_social = 1.82, onet_enterprising = 1.98, onet_conventional = 5.37 WHERE id = 1007;
-- Engenharia Aeronáutica: 17-2011.00 Aerospace Engineers
UPDATE career SET onet_code = '17-2011.00', onet_realistic = 5.65, onet_investigative = 6.05, onet_artistic = 2.35, onet_social = 1.25, onet_enterprising = 2.52, onet_conventional = 4.44 WHERE id = 1008;
-- Agronomia: 19-1013.00 Soil and Plant Scientists
UPDATE career SET onet_code = '19-1013.00', onet_realistic = 6.43, onet_investigative = 6.71, onet_artistic = 2.11, onet_social = 1.67, onet_enterprising = 1.71, onet_conventional = 3.41 WHERE id = 1009;
-- Antropologia: 19-3091.00 Anthropologists and Archeologists
UPDATE career SET onet_code = '19-3091.00', onet_realistic = 3.99, onet_investigative = 6.84, onet_artistic = 4.21, onet_social = 3.67, onet_enterprising = 1.63, onet_conventional = 3.19 WHERE id = 1010;
-- Ciência Política: 19-3094.00 Political Scientists
UPDATE career SET onet_code = '19-3094.00', onet_realistic = 1, onet_investigative = 6.71, onet_artistic = 3.87, onet_social = 3.42, onet_enterprising = 3.61, onet_conventional = 3.52 WHERE id = 1011;
-- Arqueologia: 19-3091.00 Anthropologists and Archeologists
UPDATE career SET onet_code = '19-3091.00', onet_realistic = 3.99, onet_investigative = 6.84, onet_artistic = 4.21, onet_social = 3.67, onet_enterprising = 1.63, onet_conventional = 3.19 WHERE id = 1012;
-- Finanças: 13-2051.00 Financial and Investment Analysts
UPDATE career SET onet_code = '13-2051.00', onet_realistic = 1, onet_investigative = 5.06, onet_artistic = 1.78, onet_social = 2.25, onet_enterprising = 5.09, onet_conventional = 5.54 WHERE id = 1013;
-- Turismo: 41-3041.00 Travel Agents
UPDATE career SET onet_code = '41-3041.00', onet_realistic = 1.15, onet_investigative = 1.51, onet_artistic = 1.9, onet_social = 4.31, onet_enterprising = 5.78, onet_conventional = 5.77 WHERE id = 1014;
-- Secretariado Executivo: 43-6011.00 Executive Secretaries and Executive Administrative Assistants
UPDATE career SET onet_code = '43-6011.00', onet_realistic = 1.32, onet_investigative = 1.94, onet_artistic = 1.35, onet_social = 3.94, onet_enterprising = 5.51, onet_conventional = 6.41 WHERE id = 1015;
-- Fotografia: 27-4021.00 Photographers
UPDATE career SET onet_code = '27-4021.00', onet_realistic = 5.54, onet_investigative = 2.18, onet_artistic = 5.28, onet_social = 1.94, onet_enterprising = 2.28, onet_conventional = 3.35 WHERE id = 1016;
-- Artes Cênicas: 27-2011.00 Actors
UPDATE career SET onet_code = '27-2011.00', onet_realistic = 2.29, onet_investigative = 2.16, onet_artistic = 7, onet_social = 4.08, onet_enterprising = 4, onet_conventional = 1.81 WHERE id = 1017;
-- Jogos Digitais: 15-1255.01 Video Game Designers
UPDATE career SET onet_code = '15-1255.01', onet_realistic = 2.77, onet_investigative = 4.15, onet_artistic = 6.38, onet_social = 2.3, onet_enterprising = 3.92, onet_conventional = 3.85 WHERE id = 1018;
-- Educação Especial: 25-2055.00 Special Education Teachers, Kindergarten
UPDATE career SET onet_code = '25-2055.00', onet_realistic = 2.55, onet_investigative = 3.8, onet_artistic = 3.65, onet_social = 7, onet_enterprising = 1.73, onet_conventional = 3.67 WHERE id = 1019;
-- Licenciatura em Química: 25-1052.00 Chemistry Teachers, Postsecondary
UPDATE career SET onet_code = '25-1052.00', onet_realistic = 4.05, onet_investigative = 5.82, onet_artistic = 2.95, onet_social = 5.71, onet_enterprising = 2.09, onet_conventional = 3.78 WHERE id = 1020;
-- Licenciatura em Artes Visuais: 25-1121.00 Art, Drama, and Music Teachers, Postsecondary
UPDATE career SET onet_code = '25-1121.00', onet_realistic = 3.03, onet_investigative = 4.14, onet_artistic = 5.73, onet_social = 6.99, onet_enterprising = 2.12, onet_conventional = 2.61 WHERE id = 1021;
