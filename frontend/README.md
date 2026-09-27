# Front-end — Bússola Acadêmica

Angular 18 (standalone components), visual baseado no protótipo do Figma.

```
src/app
├── core        modelos e serviços HTTP (comunicação com a API)
├── shared      componentes reutilizáveis (ex.: barra superior)
└── features    telas: home, teste-vocacional/questionario, teste-vocacional/resultado
```

A URL da API fica em `src/environments/` (`environment.development.ts` no `npm start`,
`environment.ts` no build de produção).

```bash
npm install
npm start      # http://localhost:4200 (precisa do back-end em localhost:8080)
npm test       # Jasmine/Karma
npm run build  # gera dist/frontend/browser (publicado no S3)
```
