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
├── integration  clientes de APIs externas (YouTube)
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
| POST | `/api/auth/register` | público | cadastro (exige aceite do Termo e da Política) → JWT |
| POST | `/api/auth/login` | público | login → JWT |
| GET | `/api/users/me` | autenticado | dados do próprio usuário |
| GET | `/api/users/me/results` | autenticado | histórico de resultados do usuário |
| DELETE | `/api/users/me` | autenticado | exclui a conta e os resultados (LGPD) |
| GET | `/api/vocational-test/questions` | autenticado | lista as 12 perguntas do questionário |
| POST | `/api/vocational-test/results` | autenticado | recebe as 12 respostas, calcula e salva o resultado (201) |
| GET | `/api/vocational-test/results/{id}` | dono ou admin | consulta um resultado salvo |
| GET | `/api/videos?area=` | autenticado | vídeos do YouTube sobre a área |
| GET | `/api/admin/audit-logs` | admin | logs de auditoria (filtros `action`, `email`, `page`, `size`) |
| GET | `/actuator/health` | público | health check usado no servidor |

Autenticação: enviar o token no cabeçalho `Authorization: Bearer <token>`.

Erros seguem sempre o formato `{ status, message, details, timestamp }`.
