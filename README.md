# Bússola Acadêmica

Projeto Final de Curso (PFC) — Bacharelado em Engenharia de Software (UMC), por Rayane da Luz
Barbosa. Plataforma web que centraliza informações sobre as formas de ingresso no ensino superior
(Enem/Sisu, Prouni, Fies e vestibulares) e oferece orientação vocacional inicial a estudantes
concluintes do ensino médio.

## Arquitetura

Monólito em camadas (Controller → Service → Repository), com:

| Parte | Tecnologia | Hospedagem |
| --- | --- | --- |
| Front-end | Angular 18 | AWS S3 + CloudFront |
| Back-end | Spring Boot 3.3 (Java 21), Docker | AWS EC2 + nginx (HTTPS) |
| Banco | PostgreSQL + Flyway | Neon |

Protótipo: [Figma](https://www.figma.com/design/TcdjgXfxrIpZVJAwjXlsDy/Prot%C3%B3tipo-B%C3%BAssola-Acad%C3%AAmica)

```
backend/    API REST (detalhes em backend/README.md)
frontend/   aplicação Angular (detalhes em frontend/README.md)
infra/      configuração do nginx do servidor
```

## Funcionalidades entregues

- **Teste vocacional RIASEC (Holland)** — questionário de 12 afirmações (2 por categoria, escala
  de 1 a 5). O escore de cada categoria é a soma das suas 2 respostas (2 a 10); a(s) categoria(s)
  de maior escore formam o perfil predominante e as áreas recomendadas são a união das áreas dessas
  categorias. O resultado fica salvo no banco.

## Rodando localmente

Pré-requisitos: Java 21, Maven, Node.js 20+ e um banco PostgreSQL no Neon (a connection string
fica em `backend/.env`, que não é versionado; modelo em `backend/.env.example`).

```bash
# back-end (terminal 1)
cd backend
cp .env.example .env      # primeira vez: preencha com os dados do Neon
mvn spring-boot:run       # http://localhost:8080

# front-end (terminal 2)
cd frontend
npm install
npm start                 # http://localhost:4200
```

## Testes

```bash
cd backend && mvn verify   # JUnit 5 + Mockito + JaCoCo (mínimo de 50% de cobertura)
cd frontend && npm test    # Jasmine/Karma
```

## Fluxo de trabalho

- `main`: versão estável (publicada automaticamente na AWS).
- `develop`: integração.
- `feature/...`: uma branch por entrega; PRs para `develop` e de `develop` para `main` abrem
  automaticamente pelo GitHub Actions, que também roda build e testes a cada push.

## Deploy

Todo merge na `main` publica automaticamente pelo GitHub Actions:

- `deploy-backend.yml`: gera a imagem Docker do back-end, publica no GitHub Container Registry e
  atualiza o container na EC2 (atrás do nginx com HTTPS — ver `infra/nginx`).
- `deploy-frontend.yml`: faz o build de produção do Angular, envia para o S3 e invalida o cache do
  CloudFront.

Secrets necessários no repositório: `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`, `AWS_ACCESS_KEY_ID`,
`AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, `AWS_S3_BUCKET` e `AWS_CLOUDFRONT_DISTRIBUTION_ID`. Enquanto
não existem, os workflows de deploy são pulados. Na EC2, as variáveis da API (banco e CORS) ficam em
`/opt/bussola/.env`.
