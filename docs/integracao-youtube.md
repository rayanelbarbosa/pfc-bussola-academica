# Integração com API externa — YouTube Data API v3

## Objetivo

Depois do teste vocacional, a tela de resultado mostra vídeos públicos sobre cada **área recomendada**
(ex.: perfil Investigativo → vídeos sobre Ciências Exatas, Ciências Biológicas e Medicina). Assim o
estudante conhece o dia a dia das carreiras antes de escolher o curso. A mesma integração será
reaproveitada na página de cada curso do catálogo.

## Visão geral

```
Angular (tela de resultado)
   │  GET /api/videos?area=Medicina        (JWT no cabeçalho Authorization)
   ▼
VideoController ──> VideoService ──(cache 6 h)──> YouTubeClient
                                                     │  GET https://www.googleapis.com/youtube/v3/search
                                                     ▼
                                              YouTube Data API v3
```

- O front-end **nunca** chama o YouTube diretamente: a chave da API fica só no back-end.
- `YouTubeClient` é o único componente que conhece o formato da API externa. Ele converte a resposta
  para o DTO interno `VideoResponse`, então uma mudança na API do Google afeta só essa classe.

## Chamada à API externa

| Item | Valor |
| --- | --- |
| Endpoint | `GET https://www.googleapis.com/youtube/v3/search` |
| Autenticação | chave de API (`key`), criada no Google Cloud Console e restrita à YouTube Data API v3 |
| `part` | `snippet` |
| `type` | `video` |
| `q` | `<área> carreira profissão` (ex.: `Medicina carreira profissão`) |
| `maxResults` | `3` (configurável em `app.youtube.max-results`) |
| `regionCode` / `relevanceLanguage` | `BR` / `pt` |
| `safeSearch` | `strict` (público inclui adolescentes) |

Campos usados da resposta: `items[].id.videoId`, `items[].snippet.title`, `items[].snippet.channelTitle`
e `items[].snippet.thumbnails.medium.url`. Os títulos vêm com entidades HTML (ex.: `&quot;`) e são
convertidos para texto normal.

## Endpoint próprio exposto ao front-end

`GET /api/videos?area={nome da área}` — exige usuário autenticado.

```json
[
  {
    "videoId": "abc123",
    "title": "Um dia na faculdade de Medicina",
    "channelTitle": "Canal Carreiras",
    "thumbnailUrl": "https://i.ytimg.com/vi/abc123/mqdefault.jpg",
    "url": "https://www.youtube.com/watch?v=abc123"
  }
]
```

| Situação | Resposta |
| --- | --- |
| Sucesso | `200` com a lista (pode ser vazia) |
| `area` ausente ou em branco | `400` |
| Sem token / token expirado | `401` |
| Chave não configurada ou erro/indisponibilidade do YouTube | `502` com mensagem amigável |

No front-end, as áreas são buscadas em paralelo e a falha de uma não impede as outras; se todas
falharem, a tela avisa e o resultado do teste continua sendo exibido normalmente.

## Cota e cache

A cota gratuita é de **10.000 unidades por dia** e cada busca (`search.list`) custa **100 unidades**,
ou seja, cerca de 100 buscas por dia. Para economizar, o `VideoService` guarda o resultado de cada área
em cache em memória por **6 horas** (`app.youtube.cache-hours`): várias pessoas com o mesmo perfil
não gastam cota de novo. Existem só 18 áreas possíveis no teste, então o uso diário fica bem abaixo
do limite.

## Configuração

| Variável | Onde | Descrição |
| --- | --- | --- |
| `YOUTUBE_API_KEY` | `backend/.env` (local) e `/opt/bussola/.env` (EC2) | chave da API; nunca versionada |

Propriedades em `application.properties`: `app.youtube.base-url`, `app.youtube.max-results` e
`app.youtube.cache-hours`.

## Segurança e LGPD

- A chave fica apenas no servidor (variável de ambiente) e é restrita, no Google Cloud, à YouTube Data API v3.
- **Nenhum dado pessoal é enviado ao Google**: a requisição leva só o nome da área.
- Os vídeos são exibidos como links (miniatura + título), sem player incorporado: o YouTube só recebe
  dados do usuário se ele clicar para assistir. Isso está descrito na Política de Privacidade.
- Toda busca é registrada no log de auditoria (`VIDEOS_SEARCHED`), indicando se veio do cache ou da API
  e se houve falha.

## Testes

- `YouTubeClientTest`: simula a resposta real da API (`MockRestServiceServer`) e valida o mapeamento e
  o tratamento de erro HTTP.
- `VideoServiceTest`: cache por área, chave ausente, falha da API e validação da área.
- `AccessControlIntegrationTest`: endpoint exige login e responde `502`/`400` nos cenários de erro.
