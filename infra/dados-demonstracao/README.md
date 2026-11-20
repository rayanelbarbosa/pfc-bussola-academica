# Dados de demonstração (#154)

`carga-dados-demonstracao.sql` cria **1000 cadastros fictícios** de estudantes e os testes vocacionais deles,
espalhados entre 01/06/2026 e 19/11/2026, para o dashboard e os filtros do painel administrativo terem dados
realistas. O script foi gerado com apoio de IA, a pedido do professor, e revisado e testado em um PostgreSQL 16 local.

- Todos os e-mails são do domínio `@demo.bussolaacademica.com.br` (do projeto): nenhum é de pessoa real e todos
  podem ser identificados por ele.
- Ninguém consegue entrar nessas contas (o hash é de uma senha aleatória que foi descartada).
- Os escores seguem a regra do sistema: soma das 5 respostas (0 a 4) de cada tipo, perfil = maior(es) escore(s).
- A auditoria não é alterada (ela só registra ações reais e é protegida pela corrente de hashes).

## Como rodar

1. No Neon, crie antes um branch de segurança do banco de produção (ex.: `antes-da-carga-demo`).
2. Neon > SQL Editor (banco de produção) > cole o arquivo inteiro > **Run**.
3. A última consulta mostra os cadastros e testes por mês. Rodar de novo não duplica: o script para com um aviso.

Para uma carga complementar (ex.: na véspera da apresentação, cobrindo os dias depois da primeira carga), troque
só as 3 linhas marcadas com `>>>` no script (data inicial, data final e quantidade) e rode de novo.

As duas linhas comentadas no fim do arquivo desfazem só a carga de demonstração, se precisar.
