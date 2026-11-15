# Evidências de verificação (15/11/2026)

Medições dos requisitos não funcionais feitas sobre o commit `ac2da99` (branch `entrega1611`).
Nenhum arquivo contém senhas, tokens ou dados pessoais reais.

| Requisito | Como foi medido | Resultado | Critério | Situação |
| --- | --- | --- | --- | --- |
| RNF08 Desempenho | k6, 50 usuários simultâneos por 1 min, só leitura, na EC2 contra a API (`k6-rnf08.js`) | p95 = 678 ms, média = 290 ms, 6.241 requisições, 0% de erros | p95 ≤ 2 s e < 1% de erros | Atendido |
| RNF11 Acessibilidade | Lighthouse 13 (headless), página inicial e login | Acessibilidade 100 e 100 (desempenho 63 e 77 com emulação de celular em 4G lento; boas práticas 100; SEO 91) | ≥ 90 | Atendido |
| RNF12 Compatibilidade | Teste manual: entrar → teste → resultado → Meus dados → sair | Chrome, Edge, Firefox e Safari (versões atuais em 15/11/2026) sem erros | 4 navegadores | Atendido |
| RNF14 Testabilidade | `mvn verify` (JaCoCo) e `npm test -- --code-coverage` | Back-end: 143 testes, 0 falhas, 96,0% das instruções (8.446 de 8.797). Front-end: 149 testes, 96,3% das instruções e 96,7% das linhas | ≥ 50% | Atendido |

## Arquivos

- `k6-rnf08.js`: script do teste de carga (uso no cabeçalho do arquivo).
- `k6-rnf08-resultado.txt`: saída completa do k6.
- `lighthouse-home.report.html` / `.json` e `lighthouse-login.report.html` / `.json`: relatórios do Lighthouse.

## Observações

- O teste de carga foi executado na própria instância EC2 contra `127.0.0.1:8080`, para medir a aplicação e o banco
  no ambiente real sem o limite de requisições por IP do nginx (10 req/s na API), que bloquearia 50 usuários vindos
  de um único endereço. Com a API rodando num computador no Brasil, a distância até o banco (us-east-1) domina o
  tempo de resposta e não representa a produção.
- O login do teste usa uma conta de estudante criada só para isso; as requisições são apenas de leitura.
