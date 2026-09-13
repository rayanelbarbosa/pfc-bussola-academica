#!/bin/bash
set -e

echo "== 1/14: estrutura inicial do backend (Spring Boot) =="
git add backend/pom.xml backend/.gitignore \
        backend/src/main/resources/application.properties \
        backend/src/main/java/br/com/bussolaacademica/BussolaAcademicaApplication.java
git commit -m "chore: estrutura inicial do projeto Spring Boot"

echo "== 2/14: modelo de dominio do teste vocacional (RIASEC) =="
git add backend/src/main/java/br/com/bussolaacademica/testevocacional/CategoriaRiasec.java \
        backend/src/main/java/br/com/bussolaacademica/testevocacional/Pergunta.java \
        backend/src/main/java/br/com/bussolaacademica/testevocacional/BancoDePerguntas.java
git commit -m "feat: modelo de dominio das categorias RIASEC e banco de perguntas"

echo "== 3/14: DTOs de entrada e saida da API =="
git add backend/src/main/java/br/com/bussolaacademica/testevocacional/RespostaDTO.java \
        backend/src/main/java/br/com/bussolaacademica/testevocacional/SubmeterTesteRequest.java \
        backend/src/main/java/br/com/bussolaacademica/testevocacional/ResultadoTesteResponse.java
git commit -m "feat: DTOs de requisicao e resposta do teste vocacional"

echo "== 4/14: persistencia do resultado do teste =="
git add backend/src/main/java/br/com/bussolaacademica/testevocacional/ResultadoTeste.java \
        backend/src/main/java/br/com/bussolaacademica/testevocacional/ResultadoTesteRepository.java
git commit -m "feat: entidade e repositorio JPA do resultado do teste vocacional"

echo "== 5/14: regra de negocio - calculo da pontuacao vocacional =="
git add backend/src/main/java/br/com/bussolaacademica/testevocacional/TesteVocacionalService.java \
        backend/src/main/java/br/com/bussolaacademica/testevocacional/PerguntaInvalidaException.java
git commit -m "feat: regra de negocio de pontuacao do teste vocacional (RIASEC)"

echo "== 6/14: endpoints REST e CORS =="
git add backend/src/main/java/br/com/bussolaacademica/testevocacional/TesteVocacionalController.java \
        backend/src/main/java/br/com/bussolaacademica/config/WebConfig.java
git commit -m "feat: endpoints REST do teste vocacional e configuracao de CORS"

echo "== 7/14: testes automatizados do backend =="
git add backend/src/test/java/br/com/bussolaacademica/testevocacional/TesteVocacionalServiceTest.java \
        backend/src/test/java/br/com/bussolaacademica/testevocacional/TesteVocacionalControllerTest.java \
        backend/src/test/resources/application.properties
git commit -m "test: testes unitarios e de integracao da regra de negocio"

echo "== 8/14: pipeline de CI do backend =="
git add .github/workflows/backend-ci.yml
git commit -m "ci: pipeline de build e testes do backend"

echo "== 9/14: estrutura inicial do projeto Angular =="
git add frontend/angular.json frontend/package.json frontend/package-lock.json \
        frontend/tsconfig.json frontend/tsconfig.app.json frontend/tsconfig.spec.json \
        frontend/.editorconfig frontend/.gitignore frontend/README.md \
        frontend/.vscode/extensions.json frontend/.vscode/launch.json frontend/.vscode/tasks.json \
        frontend/public/favicon.ico \
        frontend/src/main.ts frontend/src/index.html frontend/src/styles.css \
        frontend/src/app/app.config.ts frontend/src/app/app.component.css
git commit -m "chore: estrutura inicial do projeto Angular"

echo "== 10/14: modelos e servico de integracao com a API =="
git add frontend/src/app/models/teste-vocacional.models.ts \
        frontend/src/app/teste-vocacional.service.ts
git commit -m "feat: modelos e servico HTTP de integracao com a API do teste vocacional"

echo "== 11/14: componente do questionario vocacional =="
git add frontend/src/app/teste-vocacional/teste-vocacional.component.ts \
        frontend/src/app/teste-vocacional/teste-vocacional.component.html \
        frontend/src/app/teste-vocacional/teste-vocacional.component.css \
        frontend/src/app/app.component.ts frontend/src/app/app.component.html
git commit -m "feat: componente do questionario vocacional e tela de resultado"

echo "== 12/14: testes automatizados do frontend =="
git add frontend/karma.conf.js \
        frontend/src/app/teste-vocacional.service.spec.ts \
        frontend/src/app/teste-vocacional/teste-vocacional.component.spec.ts \
        frontend/src/app/app.component.spec.ts
git commit -m "test: testes do servico e do componente do teste vocacional"

echo "== 13/14: pipeline de CI do frontend =="
git add .github/workflows/frontend-ci.yml
git commit -m "ci: pipeline de build e testes do frontend"

echo "== 14/14: README do projeto =="
git add README.md
git commit -m "docs: README com visao geral do projeto e instrucoes de uso"

if [ -n "$(git status --porcelain)" ]; then
  echo "== Extra: arquivos que sobraram fora do plano (inclui este proprio script) =="
  git add -A
  git commit -m "chore: ajustes finais"
fi

echo ""
echo "Pronto! Historico criado:"
git log --oneline
