# Bússola Acadêmica

Projeto Final de Curso (PFC) — plataforma de orientação vocacional e acadêmica.

Este repositório contém a primeira regra de negócio completa implementada de ponta a ponta
(back-end + front-end + banco de dados), conforme exigido na entrega de 14/09:

**RN: Pontuação do teste vocacional**, com base no modelo científico público RIASEC (Holland, 1959).
O usuário responde a um questionário fixo de 12 perguntas (2 por categoria RIASEC), o back-end soma
as notas por categoria, identifica a(s) categoria(s) predominante(s) e retorna as áreas/cursos de
graduação recomendados, persistindo o resultado no banco de dados.

## Estrutura do repositório

```
backend/    Spring Boot 3 (Java 21) — API REST + JPA/Hibernate + PostgreSQL
frontend/   Angular 18 — questionário e tela de resultado
```

## Back-end

- Stack: Spring Boot 3.3.4, Java 21, Spring Data JPA, Bean Validation, PostgreSQL (produção) / H2 (testes).
- Regra de negócio: `src/main/java/br/com/bussolaacademica/testevocacional/TesteVocacionalService.java`
- Endpoints:
  - `GET /api/teste-vocacional/perguntas` — lista as 12 perguntas fixas.
  - `POST /api/teste-vocacional` — recebe as respostas, calcula e persiste o resultado.
- Testes: `mvn test` (JUnit 5 + AssertJ + MockMvc, banco H2 em memória). Cobertura via JaCoCo (`mvn verify`, relatório em `target/site/jacoco`).

```bash
cd backend
mvn spring-boot:run   # sobe em http://localhost:8080
mvn verify             # roda os testes e gera o relatório de cobertura
```

Configuração do banco de produção via variáveis de ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`) —
ver `src/main/resources/application.properties`.

## Front-end

- Stack: Angular 18, standalone components, HttpClient.
- Componente principal: `src/app/teste-vocacional/teste-vocacional.component.ts`
- Serviço de integração com a API: `src/app/teste-vocacional.service.ts`

```bash
cd frontend
npm install
npm start        # http://localhost:4200
npm test         # Karma/Jasmine
```

## CI (GitHub Actions)

Ao dar push neste repositório, dois workflows rodam automaticamente
(`.github/workflows/backend-ci.yml` e `frontend-ci.yml`): build, testes e relatório
de cobertura de cada projeto. Confira a aba **Actions** do GitHub após o push —
o check verde é a confirmação objetiva de que o código compila e os testes passam.

## Observação sobre o ambiente de desenvolvimento

O back-end foi desenvolvido e revisado manualmente em um ambiente sem acesso ao Maven
Central, então a compilação/execução dos testes deve ser conferida localmente (`mvn verify`)
ou pelo GitHub Actions após o push, antes da entrega.
