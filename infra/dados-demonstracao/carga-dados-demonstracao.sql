-- =====================================================================================================================
-- Bússola Acadêmica: carga de dados de demonstração (#154)
--
-- Cria 1000 cadastros FICTÍCIOS de estudantes e os testes vocacionais deles, espalhados entre 01/06/2026 e
-- 19/11/2026, para o dashboard e os filtros do painel administrativo terem dados realistas na apresentação.
--
-- Como os dados são gerados:
--   * Dias: crescimento gradual ao longo do período, menos cadastros no fim de semana e mais movimento na reta
--     final antes do Enem (a partir de 20/10). Nenhum dia concentra a carga: cada um recebe poucos cadastros.
--   * Hora: entre 7h e 22h59.
--   * Nome: combinação de nomes e sobrenomes comuns no Brasil (pessoas fictícias).
--   * E-mail: sempre no domínio @demo.bussolaacademica.com.br (do próprio projeto), então nenhum é de pessoa real
--     e todos podem ser identificados por esse domínio.
--   * Senha: hash BCrypt de uma senha aleatória que foi descartada. Ninguém consegue entrar nessas contas.
--   * Termos: a versão vigente na data do cadastro (tabela terms_version), aceita no momento do cadastro.
--   * Teste: cerca de 85% dos alunos fazem o teste, e uns 12% deles refazem depois de alguns dias. As 30 respostas
--     (0 a 4) são sorteadas com 1 ou 2 tipos RIASEC preferidos; o escore de cada tipo é a soma das 5 respostas
--     (0 a 20) e o perfil principal são os tipos com o maior escore (empates entram juntos), igual ao back-end.
--   * Áreas de interesse: de 1 a 3, quase sempre ligadas ao tipo preferido; cerca de 10% escolhem "Ainda não sei".
--
-- Fica de fora de propósito: a auditoria. Ela só registra ações reais e é protegida por uma corrente de hashes;
-- inserir eventos por fora quebraria o "Verificar integridade" (e seria registrar algo que não aconteceu).
--
-- Como rodar: Neon > SQL Editor (banco de produção) > colar o arquivo inteiro > Run. Leva poucos segundos.
-- Rodar de novo não duplica nada: o script para com um aviso se já encontrar os dados de demonstração.
-- O sorteio usa uma semente fixa (setseed), então a carga sai sempre igual.
-- =====================================================================================================================

DO $$
DECLARE
    demo_domain  CONSTANT text := '@demo.bussolaacademica.com.br';
    start_day    CONSTANT date := DATE '2026-06-01';
    end_day      CONSTANT date := DATE '2026-11-19';
    demo_hash    CONSTANT text := '$2a$10$kJWpAKGX.JAwORw6zLcwueCLTKbmu5Qryjb6ib.wyhwQmA1IMWWNa';
    total_users  CONSTANT int  := 1000;

    first_names  text[] := ARRAY['Ana', 'Maria', 'Julia', 'Beatriz', 'Larissa', 'Camila', 'Fernanda', 'Gabriela',
        'Mariana', 'Isabela', 'Leticia', 'Amanda', 'Bruna', 'Carolina', 'Vitoria', 'Rafaela', 'Yasmin', 'Luana',
        'Bianca', 'Giovanna', 'Lucas', 'Gabriel', 'Pedro', 'Mateus', 'Guilherme', 'Rafael', 'Felipe', 'Gustavo',
        'Joao', 'Vinicius', 'Leonardo', 'Thiago', 'Bruno', 'Daniel', 'Eduardo', 'Henrique', 'Caio', 'Diego',
        'Samuel', 'Arthur', 'Enzo', 'Davi', 'Kaua', 'Nicolas', 'Igor', 'Renan', 'Murilo', 'Otavio'];
    last_names   text[] := ARRAY['Silva', 'Santos', 'Oliveira', 'Souza', 'Rodrigues', 'Ferreira', 'Alves', 'Pereira',
        'Lima', 'Gomes', 'Costa', 'Ribeiro', 'Martins', 'Carvalho', 'Almeida', 'Lopes', 'Soares', 'Fernandes',
        'Vieira', 'Barbosa', 'Rocha', 'Dias', 'Nascimento', 'Andrade', 'Moreira', 'Nunes', 'Marques', 'Machado',
        'Mendes', 'Freitas', 'Cardoso', 'Ramos', 'Goncalves', 'Santana', 'Teixeira', 'Moura', 'Correia', 'Pinto'];

    -- Tipos RIASEC na ordem oficial do back-end (R, I, A, S, E, C) e quanto cada um aparece como preferido.
    types        text[] := ARRAY['REALISTIC', 'INVESTIGATIVE', 'ARTISTIC', 'SOCIAL', 'ENTERPRISING', 'CONVENTIONAL'];
    type_weight  int[]  := ARRAY[12, 22, 18, 24, 14, 10];
    -- Áreas de interesse mais ligadas a cada tipo (mesma ordem de "types").
    affinity     text[] := ARRAY[
        ['ENGINEERING', 'EXACT_AND_TECHNOLOGY', 'HEALTH'],
        ['HEALTH', 'EXACT_AND_TECHNOLOGY', 'ENGINEERING'],
        ['ARTS_AND_COMMUNICATION', 'HUMANITIES', 'EDUCATION'],
        ['EDUCATION', 'HEALTH', 'HUMANITIES'],
        ['BUSINESS', 'HUMANITIES', 'ARTS_AND_COMMUNICATION'],
        ['BUSINESS', 'EXACT_AND_TECHNOLOGY', 'HEALTH']];
    all_areas    text[] := ARRAY['HEALTH', 'EXACT_AND_TECHNOLOGY', 'ENGINEERING', 'HUMANITIES', 'BUSINESS',
        'ARTS_AND_COMMUNICATION', 'EDUCATION'];

    i int; k int; t int; q int; n int;
    roll double precision;
    chosen_day date;
    created timestamp;
    test_at timestamp;
    user_name text;
    new_user_id bigint;
    dom1 int; dom2 int;
    mean double precision;
    scores int[];
    best int;
    tops text[];
    areas text[];
    area text;
BEGIN
    PERFORM setseed(0.2026);

    IF EXISTS (SELECT 1 FROM app_user WHERE email LIKE '%' || demo_domain) THEN
        RAISE EXCEPTION 'Os dados de demonstração já foram carregados (há e-mails %). Nada foi alterado.', demo_domain;
    END IF;

    -- Peso de cada dia: cresce ao longo do período, cai no fim de semana e sobe na reta final do Enem.
    CREATE TEMP TABLE demo_day ON COMMIT DROP AS
    SELECT d::date AS day,
           (1.0 + 2.0 * (d::date - start_day) / (end_day - start_day))
               * (CASE WHEN extract(isodow FROM d) IN (6, 7) THEN 0.55 ELSE 1.0 END)
               * (CASE WHEN d::date >= DATE '2026-10-20' THEN 1.5 ELSE 1.0 END)
               * (0.7 + random() * 0.6) AS weight
    FROM generate_series(start_day, end_day, INTERVAL '1 day') AS d;

    CREATE TEMP TABLE demo_day_cdf ON COMMIT DROP AS
    SELECT dd.day, sum(dd.weight) OVER (ORDER BY dd.day) / sum(dd.weight) OVER () AS cumulative FROM demo_day dd;

    FOR i IN 1..total_users LOOP
        -- dia sorteado pelo peso e hora entre 7h e 22h59
        roll := random();
        SELECT c.day INTO chosen_day FROM demo_day_cdf c WHERE c.cumulative >= roll ORDER BY c.cumulative LIMIT 1;
        created := chosen_day + make_interval(hours => 7 + floor(random() * 16)::int, mins => floor(random() * 60)::int,
                                       secs => floor(random() * 60)::int);

        user_name := first_names[1 + floor(random() * array_length(first_names, 1))::int] || ' '
                  || last_names[1 + floor(random() * array_length(last_names, 1))::int];

        INSERT INTO app_user (name, email, password_hash, role, terms_accepted_at, terms_version, created_at)
        VALUES (user_name,
                lower(translate(replace(user_name, ' ', '.'), 'áàâãéêíóôõúç', 'aaaaeeiooouc')) || '.' || i || demo_domain,
                demo_hash, 'STUDENT', created,
                coalesce((SELECT v.version FROM terms_version v WHERE v.published_at <= created
                          ORDER BY v.published_at DESC LIMIT 1),
                         (SELECT v.version FROM terms_version v ORDER BY v.published_at LIMIT 1)),
                created)
        RETURNING id INTO new_user_id;

        CONTINUE WHEN random() >= 0.85;           -- cerca de 15% se cadastram e não fazem o teste

        -- tipo preferido (sorteado pelo peso) e, às vezes, um segundo
        roll := random() * 100;
        dom1 := 1;
        WHILE roll >= type_weight[dom1] LOOP
            roll := roll - type_weight[dom1];
            dom1 := dom1 + 1;
        END LOOP;
        dom2 := CASE WHEN random() < 0.5 THEN 1 + floor(random() * 6)::int ELSE 0 END;

        test_at := created + make_interval(mins => 2 + floor(random() * 38)::int);
        FOR k IN 1..(CASE WHEN random() < 0.12 THEN 2 ELSE 1 END) LOOP
            IF k = 2 THEN
                test_at := test_at + make_interval(days => 1 + floor(random() * 30)::int);
                EXIT WHEN test_at::date > end_day;
            END IF;

            -- 5 respostas de 0 a 4 por tipo; escore = soma (0 a 20)
            scores := ARRAY[0, 0, 0, 0, 0, 0];
            FOR t IN 1..6 LOOP
                mean := CASE WHEN t = dom1 THEN 3.0 WHEN t = dom2 THEN 2.4 ELSE 0.9 + random() * 1.3 END;
                FOR q IN 1..5 LOOP
                    scores[t] := scores[t] + least(4, greatest(0, round(mean + (random() - 0.5) * 2.4)))::int;
                END LOOP;
            END LOOP;

            -- perfil principal: todos os tipos com o maior escore, na ordem oficial
            SELECT max(s) INTO best FROM unnest(scores) AS s;
            tops := ARRAY[]::text[];
            FOR t IN 1..6 LOOP
                IF scores[t] = best THEN
                    tops := tops || types[t];
                END IF;
            END LOOP;

            -- áreas de interesse: "Ainda não sei" (10%) ou de 1 a 3 áreas, a 1ª quase sempre ligada ao tipo preferido
            IF random() < 0.10 THEN
                areas := ARRAY['UNDECIDED'];
            ELSE
                areas := ARRAY[]::text[];
                n := 1 + floor(random() * 3)::int;
                WHILE array_length(areas, 1) IS DISTINCT FROM n LOOP
                    area := CASE WHEN random() < 0.75 THEN affinity[dom1][1 + floor(random() * 3)::int]
                                 ELSE all_areas[1 + floor(random() * 7)::int] END;
                    IF NOT area = ANY (areas) THEN
                        areas := areas || area;
                    END IF;
                END LOOP;
            END IF;

            INSERT INTO vocational_test_result (created_at, realistic_score, investigative_score, artistic_score,
                                                social_score, enterprising_score, conventional_score, top_categories,
                                                interest_areas, max_score, user_id)
            VALUES (test_at, scores[1], scores[2], scores[3], scores[4], scores[5], scores[6],
                    array_to_string(tops, ';'), array_to_string(areas, ';'), 20, new_user_id);
        END LOOP;
    END LOOP;

    RAISE NOTICE 'Carga concluída: % cadastros e % testes de demonstração.',
        (SELECT count(*) FROM app_user WHERE email LIKE '%' || demo_domain),
        (SELECT count(*) FROM vocational_test_result vtr JOIN app_user au ON au.id = vtr.user_id
         WHERE au.email LIKE '%' || demo_domain);
END $$;

-- Conferência: cadastros e testes de demonstração por mês (nenhum mês concentra a carga).
SELECT to_char(u.created_at, 'YYYY-MM') AS mes,
       count(DISTINCT u.id)            AS cadastros,
       count(r.id)                     AS testes,
       max(per_day.n)                  AS maior_dia
FROM app_user u
LEFT JOIN vocational_test_result r ON r.user_id = u.id
LEFT JOIN LATERAL (SELECT count(*) AS n FROM app_user x
                   WHERE x.email LIKE '%@demo.bussolaacademica.com.br'
                     AND x.created_at::date = u.created_at::date) per_day ON true
WHERE u.email LIKE '%@demo.bussolaacademica.com.br'
GROUP BY 1
ORDER BY 1;

-- Para desfazer só a carga de demonstração (sem tocar em nenhum outro cadastro), rode as 2 linhas abaixo sem o "--":
-- DELETE FROM vocational_test_result WHERE user_id IN (SELECT id FROM app_user WHERE email LIKE '%@demo.bussolaacademica.com.br');
-- DELETE FROM app_user WHERE email LIKE '%@demo.bussolaacademica.com.br';
