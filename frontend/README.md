# Front-end — Bússola Acadêmica

Angular 18 (standalone components), estilizado com Tailwind CSS.

```
src/app
├── core        modelos, serviços HTTP, guards e interceptor (token, sessão expirada e novo aceite dos termos)
├── shared      componentes reutilizáveis (barra superior, rodapé, gráficos do dashboard)
└── features    telas: home (formas de ingresso), auth (login, cadastro, recuperação de senha),
                vocational-test (áreas, questionário e resultado), account (meus dados e meus resultados),
                admin (formas de ingresso, usuários, termos, auditoria e dashboard),
                legal (Termo de Uso e Política de Privacidade) e errors (acesso negado)
```

A URL da API fica em `src/environments/` (`environment.development.ts` no `npm start`,
`environment.ts` no build de produção).

```bash
npm install
npm start      # http://localhost:4200 (precisa do back-end em localhost:8080)
npm test       # Jasmine/Karma
npm run build  # gera dist/frontend/browser (publicado no S3)
```
