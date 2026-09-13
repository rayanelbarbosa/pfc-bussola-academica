# Back-end — Bússola Acadêmica

API REST em Spring Boot 3 (Java 21) responsável pelas regras de negócio do Bússola Acadêmica.
Nesta entrega, implementa a regra de negócio de pontuação do teste vocacional, baseada no modelo
RIASEC (Holland).

## Executando localmente

Pré-requisitos: Java 21, Maven e um PostgreSQL acessível (o mais rápido é via Docker).

```bash
docker run --name bussola-postgres \
  -e POSTGRES_USER=bussola \
  -e POSTGRES_PASSWORD=bussola \
  -e POSTGRES_DB=bussola_academica \
  -p 5432:5432 \
  -d postgres:16

mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Na primeira execução, o Hibernate cria automaticamente a
tabela `resultado_teste_vocacional` no banco (`spring.jpa.hibernate.ddl-auto=update`).

## Endpoints

- `GET /api/teste-vocacional/perguntas` — lista as 12 perguntas fixas do questionário (2 por
  categoria RIASEC).
- `POST /api/teste-vocacional` — recebe as respostas do usuário, calcula a(s) categoria(s)
  predominante(s), persiste o resultado no banco e retorna as áreas/cursos recomendados.

## Testes automatizados

```bash
mvn test     # JUnit 5 + AssertJ + MockMvc, banco H2 em memória
mvn verify   # roda os testes e gera o relatório de cobertura JaCoCo (target/site/jacoco)
```

## Estrutura principal

```
src/main/java/br/com/bussolaacademica/
├── config/            configurações gerais (CORS, etc.)
└── testevocacional/
    ├── controller      endpoints REST
    ├── service         regra de negócio (pontuação RIASEC)
    ├── repository      acesso ao banco (Spring Data JPA)
    └── domain/dto      entidades, categorias, perguntas e objetos de requisição/resposta
```

## Configuração do banco

As credenciais e a URL do banco ficam em `src/main/resources/application.properties`. Em
produção, são configuradas via variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`).

## Mais informações

Projeto gerado com o [Spring Initializr](https://start.spring.io/). Documentação oficial do
Spring Boot: https://docs.spring.io/spring-boot/
