# Front-end — Bússola Acadêmica

Aplicação Angular 18 (standalone components) responsável pelas telas do Bússola Acadêmica.
Nesta entrega, implementa o questionário do teste vocacional e a tela de resultado, consumindo a
API REST do back-end.

## Servidor de desenvolvimento

Rode `npm start` (ou `ng serve`) para subir o servidor de desenvolvimento. Acesse
`http://localhost:4200/` — a aplicação recarrega automaticamente ao salvar alterações nos
arquivos.

## Build de produção

Rode `npm run build` (ou `ng build`) para gerar o build de produção. Os arquivos ficam na pasta
`dist/`.

## Testes automatizados

Rode `npm test` (ou `ng test`) para executar os testes unitários via Jasmine/Karma.

## Estrutura principal

- `src/app/teste-vocacional/` — componente do questionário vocacional e tela de resultado.
- `src/app/teste-vocacional.service.ts` — serviço HTTP de integração com a API do back-end.
- `src/app/models/` — modelos (interfaces) usados na comunicação com a API.

## Mais informações

Este projeto foi gerado com o [Angular CLI](https://github.com/angular/angular-cli) versão
18.2.21. Para mais comandos, veja a
[documentação oficial do Angular CLI](https://angular.dev/tools/cli).
