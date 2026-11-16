# Bússola Acadêmica

Projeto Final de Curso (PFC) do Bacharelado em Engenharia de Software (UMC), por Rayane da Luz
Barbosa. Plataforma web que centraliza informações sobre as formas de ingresso no ensino superior
(Enem/Sisu, Prouni, Fies e vestibulares) e oferece orientação vocacional inicial a estudantes
concluintes do ensino médio.

**Sistema publicado:** https://www.bussolaacademica.com.br (API em https://api.bussolaacademica.com.br)

**Manual do usuário:** [docs/manual-do-usuario.pdf](docs/manual-do-usuario.pdf) · **Evidências dos requisitos não funcionais:** [docs/evidencias](docs/evidencias/README.md)

## Arquitetura

Monólito em camadas (Controller → Service → Repository), com:

| Parte | Tecnologia | Hospedagem |
| --- | --- | --- |
| Front-end | Angular 18 | AWS EC2, servido pelo nginx (HTTPS) |
| Back-end | Spring Boot 3.3 (Java 21), Docker | AWS EC2, atrás do nginx (proxy reverso) |
| Banco | PostgreSQL + Flyway | Neon |

Protótipo: [Figma](https://www.figma.com/design/TcdjgXfxrIpZVJAwjXlsDy/Prot%C3%B3tipo-B%C3%BAssola-Acad%C3%AAmica)

```
backend/    API REST (detalhes em backend/README.md)
frontend/   aplicação Angular (detalhes em frontend/README.md)
infra/      configuração do nginx do servidor
docs/       manual do usuário, integração com o YouTube e evidências (desempenho, acessibilidade, cobertura)
.github/    CI (build e testes), PRs automáticos e deploy na AWS
```

## Funcionalidades entregues

- **Páginas públicas**: página inicial, "Como funciona o teste" e **Formas de ingresso** no ensino superior (Enem, Sisu, Prouni, Fies,
  vestibulares próprios, faculdades particulares, Quero Bolsa e Educa Mais Brasil), filtro por tipo e
  link para o site oficial. O administrador inclui, altera e desativa as formas de ingresso.
- **Teste vocacional RIASEC (Holland)** com o questionário **O\*NET Mini Interest Profiler** (30 atividades,
  5 por categoria, versão validada por Rounds et al., 2016). O estudante escolhe de 1 a 3 áreas de interesse
  (ou "Ainda não sei") e responde cada atividade de 0 (detesto) a 4 (adoro). O escore de cada categoria é a
  soma das suas 5 respostas (0 a 20); a(s) categoria(s) de maior escore formam o perfil principal (empates
  são mantidos). Se os 6 escores forem iguais, o resultado avisa que as respostas não diferenciam os interesses.
- **Recomendação de carreiras** (70 carreiras, cada uma com o código de Holland de 3 letras da base O\*NET 31.0):
  as carreiras são ordenadas pelos pontos do estudante no 1º tipo do código da carreira, depois no 2º, no 3º
  e, por último, pelo nome. O resultado mostra as 4 primeiras das áreas escolhidas, o **ranking completo** de
  todas as carreiras e, quando nenhuma carreira das áreas escolhidas tem como 1º tipo um tipo do perfil, um
  **aviso de área** com as 4 que mais combinam em todas as áreas.
- **Resultado e histórico**: gráfico de radar com os 6 escores, explicação do perfil, carreiras recomendadas e
  "Meus resultados" com os testes anteriores.
- **Vídeos do YouTube** (YouTube Data API v3) sobre cada carreira, carregados sob demanda e reordenados para o
  perfil do estudante, com cache de 24 h. Detalhes em [docs/integracao-youtube.md](docs/integracao-youtube.md).
- **Cadastro, login e recuperação de senha**: JWT (HS256), senhas com BCrypt, logout com revogação do
  token, bloqueio após 5 tentativas sem sucesso em 15 minutos e redefinição de senha por e-mail (Resend)
  com link de uso único válido por 30 minutos. Ações críticas (excluir a própria conta ou a de outro
  usuário) pedem a senha de novo.
- **Perfis de acesso**: estudante e administrador, com página de acesso negado no front-end.
- **Administração**: formas de ingresso, carreiras (inclusão, alteração, consulta e desativação, com o código
  RIASEC), usuários e administradores, versões do Termo de Uso e da Política de Privacidade, dashboard de
  métricas de uso e auditoria com filtros. Os registros de auditoria são encadeados por hash, e o botão
  "Verificar integridade" aponta qualquer registro alterado ou removido.
- **LGPD**: aceite obrigatório dos termos (com novo aceite a cada versão), declaração de idade mínima de
  16 anos, página "Meus dados" para consultar, exportar (JSON) e excluir a conta (os resultados do teste são
  anonimizados, sem vínculo com a pessoa), e expurgo diário dos registros de auditoria com mais de 6 meses.

### Créditos do questionário

Atividades do O\*NET® Mini Interest Profiler (O\*NET Career Exploration Tools, USDOL/ETA), traduzidas por
Rayane da Luz Barbosa. USDOL/ETA has not approved, endorsed, or tested these modifications.
[Licença](https://www.onetcenter.org/license_tools.html). Os códigos de Holland das carreiras vêm da base
O\*NET 31.0 (Career Interest Types).

## Rodando localmente

Pré-requisitos: Java 21, Maven, Node.js 20+, um banco PostgreSQL no Neon, uma chave da YouTube
Data API v3 e uma chave da Resend. As configurações ficam em `backend/.env`, que não é versionado
(modelo em `backend/.env.example`): conexão com o banco, `JWT_SECRET`, `ADMIN_EMAIL`/`ADMIN_PASSWORD`
(cria o administrador inicial), `YOUTUBE_API_KEY`, `RESEND_API_KEY` e `FRONTEND_URL`.

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

- `main`: versão estável.
- `develop`: integração.
- `feature/...`: uma branch por entrega; PRs para `develop` e de `develop` para `main` abrem
  automaticamente pelo GitHub Actions, que também roda build e testes a cada push.

## Deploy

O front-end e a API rodam na mesma instância EC2 (us-east-2). O nginx serve os arquivos do Angular em
`www.bussolaacademica.com.br`, faz proxy reverso da API em `api.bussolaacademica.com.br`, termina o HTTPS
(certificado com renovação automática) e aplica HSTS, CSP e limite de requisições (ver
`infra/nginx`). Todo merge na `main` publica automaticamente pelo GitHub Actions:

- `deploy-backend.yml`: gera a imagem Docker do back-end, publica no GitHub Container Registry e
  atualiza o container na EC2.
- `deploy-frontend.yml`: faz o build de produção do Angular, envia para a EC2 e troca a pasta publicada
  de uma vez, sem deixar o site pela metade.

Secrets necessários no repositório: `EC2_HOST`, `EC2_USER` e `EC2_SSH_KEY`. Na EC2, as variáveis da API
(banco e CORS) ficam em `/opt/bussola/.env` (as mesmas do `backend/.env.example`, com
`SPRING_PROFILES_ACTIVE=prod`).
