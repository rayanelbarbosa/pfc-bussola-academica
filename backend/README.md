# Back-end — Bússola Acadêmica

API REST em Spring Boot 3.3 (Java 21), organizada em camadas:

```
br.com.bussolaacademica
├── controller   endpoints REST (recebem e devolvem DTOs)
├── service      regras de negócio (ex.: pontuação RIASEC)
├── repository   acesso ao banco (Spring Data JPA)
├── model        entidades JPA e enums de domínio
├── dto          objetos de entrada/saída da API
├── exception    exceções de negócio + tratamento global de erros
├── security     Spring Security, JWT, respostas 401/403
├── integration  clientes de APIs externas (YouTube e Resend)
└── config       propriedades, administrador inicial e beans de configuração
```

## Convenções

Código (pacotes, classes, métodos, tabelas, endpoints e JSON) em **inglês**, padrão de mercado.
Textos exibidos ao usuário (mensagens de erro, rótulos das categorias) e comentários/Javadoc em
**português**.

## Configuração

| Perfil | Quando usar | Como ativa |
| --- | --- | --- |
| `dev` (padrão) | rodando na máquina, conectado ao Neon | automático |
| `prod` | servidor na AWS (EC2 + Docker) | `SPRING_PROFILES_ACTIVE=prod` |

As credenciais do banco **nunca** ficam no código. Localmente, copie `.env.example` para `.env`
(este arquivo não vai para o Git) e preencha com a connection string do Neon.

O schema do banco é versionado com **Flyway** (`src/main/resources/db/migration`). Para mudar uma
tabela, crie um novo arquivo `V<n>__descricao.sql` — nunca edite uma migration já aplicada.

## Rodando

```bash
cp .env.example .env   # só na primeira vez; depois edite o .env
mvn spring-boot:run     # sobe em http://localhost:8080
mvn verify              # testes + cobertura JaCoCo (falha se < 50%)
```

Relatório de cobertura: `target/site/jacoco/index.html`.

## Endpoints

| Método | Rota | Acesso | Descrição |
| --- | --- | --- | --- |
| POST | `/api/auth/register` | público | cadastro (idade mínima e aceite dos termos) → JWT |
| POST | `/api/auth/login` | público | login → JWT (bloqueio após 5 falhas em 15 min) |
| POST | `/api/auth/logout` | autenticado | revoga o token atual |
| POST | `/api/auth/password/forgot` | público | envia o link de redefinição por e-mail (resposta sempre igual) |
| POST | `/api/auth/password/reset/validate` | público | confere se o link ainda é válido |
| POST | `/api/auth/password/reset` | público | define a nova senha (link de uso único, 30 min) |
| GET | `/api/terms/current` | público | versão vigente do Termo de Uso e da Política de Privacidade |
| POST | `/api/terms/accept` | autenticado | aceita a versão vigente |
| GET | `/api/admission-methods` | público | formas de ingresso ativas (filtro `type`) |
| GET | `/api/admission-methods/types` | público | tipos de forma de ingresso |
| GET | `/api/users/me` | autenticado | dados do próprio usuário |
| GET | `/api/users/me/results` | autenticado | histórico de resultados |
| GET | `/api/users/me/export` | autenticado | exporta os dados pessoais em JSON |
| DELETE | `/api/users/me` | autenticado | exclui a conta e os resultados |
| GET | `/api/vocational-test/questions` | autenticado | as 12 afirmações do questionário |
| GET | `/api/vocational-test/interest-areas` | autenticado | áreas de interesse disponíveis |
| POST | `/api/vocational-test/results` | autenticado | calcula e salva o resultado (201) |
| GET | `/api/vocational-test/results/{id}` | dono ou admin | consulta um resultado |
| GET | `/api/videos?career=` | autenticado | vídeos do YouTube de uma carreira cadastrada |
| GET/POST | `/api/admin/admission-methods` | admin | consulta paginada e inclusão de formas de ingresso |
| GET/PUT | `/api/admin/admission-methods/{id}` | admin | consulta e alteração |
| PATCH | `/api/admin/admission-methods/{id}/status` | admin | ativa ou desativa |
| GET/POST | `/api/admin/users` | admin | lista usuários (busca e perfil) e cadastra administrador |
| PATCH | `/api/admin/users/{id}/role` | admin | promove ou rebaixa (exceto a si mesmo) |
| DELETE | `/api/admin/users/{id}` | admin | exclui a conta de outro usuário |
| GET/POST | `/api/admin/terms` | admin | lista e publica versões dos termos |
| GET | `/api/admin/audit-logs` | admin | auditoria (filtros `action`, `email`, `page`, `size`) |
| GET | `/api/admin/audit-logs/actions` | admin | ações disponíveis no filtro |
| GET | `/api/admin/dashboard?days=` | admin | métricas de uso (7, 30 ou 90 dias) |
| GET | `/actuator/health` | público | health check |

Autenticação: enviar o token no cabeçalho `Authorization: Bearer <token>`.

Erros seguem sempre o formato `{ status, message, details, timestamp }`.
