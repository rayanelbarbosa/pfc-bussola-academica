# Bússola Acadêmica

Projeto Final de Curso (PFC) do curso de Bacharelado em Engenharia de Software — uma plataforma
web que centraliza, em um único ponto de acesso, informações sobre as formas de ingresso no
ensino superior no Brasil (Enem/Sisu, Prouni, Fies e vestibulares próprios) e oferece orientação
vocacional inicial a estudantes concluintes do ensino médio.

## Visão geral do produto

A proposta completa do Bússola Acadêmica prevê:

- Catálogo único de cursos e formas de ingresso, mantido atualizado por curadoria periódica.
- Teste vocacional com pontuação por regras, baseado no modelo científico público RIASEC (Holland).
- Página de curso com resumo da profissão e vídeos de apoio buscados via API do YouTube.
- Cadastro, login e recuperação de senha de usuários.
- Painel administrativo do catálogo, com auditoria de acessos e ações (conformidade com a LGPD).
- Dashboard de métricas de uso da plataforma.

A arquitetura é um monolito em camadas (Controller → Service → Repository), com back-end em
Spring Boot e front-end em Angular, publicado na AWS (CloudFront/S3 para o front-end, EC2 com
Docker e nginx para o back-end) e banco de dados PostgreSQL gerenciado (Neon). Mais detalhes e o
diagrama de arquitetura estão na monografia do projeto.

## Estrutura do repositório

```
backend/    Spring Boot 3 (Java 21) — API REST + JPA/Hibernate + PostgreSQL
frontend/   Angular 18 — telas da aplicação
```

## Entrega de 14/09 — primeira regra de negócio

Esta entrega implementa, de ponta a ponta (back-end + front-end + banco de dados), a primeira
regra de negócio completa do projeto:

**Pontuação do teste vocacional**, com base no modelo RIASEC (Holland, 1959). O usuário responde
a um questionário fixo de 12 perguntas (2 por categoria RIASEC), o back-end soma as notas por
categoria, identifica a(s) categoria(s) predominante(s) do perfil do usuário e retorna as
áreas/cursos de graduação recomendados, persistindo o resultado no banco de dados. Por ser um
modelo científico já validado e de domínio público, não foi necessária a participação de um
profissional de psicologia na elaboração do questionário.

## Como rodar o projeto localmente

Pré-requisitos: Java 21 (JDK), Maven, Node.js 18+ e um PostgreSQL acessível (o jeito mais rápido é
via Docker; se preferir, pode usar uma instância PostgreSQL local já instalada).

### 1. Banco de dados (PostgreSQL via Docker)

```bash
docker run --name bussola-postgres \
  -e POSTGRES_USER=bussola \
  -e POSTGRES_PASSWORD=bussola \
  -e POSTGRES_DB=bussola_academica \
  -p 5432:5432 \
  -d postgres:16
```

Esses valores já são os padrões usados pelo back-end (veja `backend/src/main/resources/application.properties`),
então não é preciso configurar mais nada se você usar exatamente esse comando.

### 2. Back-end

```bash
cd backend
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execução, o Hibernate cria automaticamente a
tabela `resultado_teste_vocacional` no banco (`spring.jpa.hibernate.ddl-auto=update`).

### 3. Front-end

Em outro terminal:

```bash
cd frontend
npm install
npm start
```

Acesse `http://localhost:4200` no navegador — é essa tela que mostra o questionário vocacional e o
resultado, útil para gravar a demonstração da entrega.

### 4. Testando de ponta a ponta

Com o banco, o back-end e o front-end rodando, abra `http://localhost:4200`, responda as 12
perguntas do teste (escala de 1 a 5) e clique em "Ver meu resultado" — a tela deve mostrar a(s)
categoria(s) predominante(s) e as áreas/cursos recomendados. Esse fluxo completo (front-end →
API → banco de dados) é o que comprova a regra de negócio funcionando de ponta a ponta.

## Testes automatizados

```bash
cd backend && mvn verify   # JUnit 5 + AssertJ + MockMvc; relatório de cobertura JaCoCo em target/site/jacoco
cd frontend && npm test    # Jasmine/Karma
```

## CI (GitHub Actions)

A cada push, dois workflows rodam automaticamente (`.github/workflows/backend-ci.yml` e
`frontend-ci.yml`): build, testes e relatório de cobertura de cada projeto. Confira a aba
**Actions** do GitHub após o push — o check verde é a confirmação objetiva de que o código
compila e os testes passam.

## Observação sobre o ambiente de desenvolvimento

O back-end foi desenvolvido e revisado manualmente em um ambiente sem acesso ao Maven Central,
então a compilação/execução dos testes foi validada localmente (`mvn verify`) e pelo GitHub
Actions após o push, antes da entrega.
