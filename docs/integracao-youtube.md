# Integração com API externa — YouTube Data API v3

## Objetivo

Depois do teste vocacional, a tela de resultado mostra vídeos públicos sobre cada **carreira
recomendada** (ex.: área Saúde → Medicina, Nutrição e Enfermagem). Assim o estudante conhece o dia a dia
das carreiras antes de escolher o curso.

## Visão geral

```
Angular (tela de resultado)
   │  GET /api/videos?career=Medicina      (JWT no cabeçalho Authorization)
   ▼
VideoController ──> VideoService ──(cache 24 h)──> YouTubeClient
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
| `q` | `<carreira> carreira profissão` (ex.: `Medicina carreira profissão`) |
| `maxResults` | `3` (configurável em `app.youtube.max-results`) |
| `regionCode` / `relevanceLanguage` | `BR` / `pt` |
| `safeSearch` | `strict` (público inclui adolescentes) |

Campos usados da resposta: `items[].id.videoId`, `items[].snippet.title`, `items[].snippet.channelTitle`
e `items[].snippet.thumbnails.medium.url`. Os títulos vêm com entidades HTML (ex.: `&quot;`) e são
convertidos para texto normal.

## Endpoint próprio exposto ao front-end

`GET /api/videos?career={nome da carreira}`: exige usuário autenticado e aceita apenas carreiras
cadastradas na tabela `career` (sem diferenciar maiúsculas e minúsculas).

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
| `career` ausente, em branco ou não cadastrada | `400` (o YouTube não é chamado) |
| Sem token / token expirado | `401` |
| Chave não configurada ou erro/indisponibilidade do YouTube | `502` com mensagem amigável |

No front-end, as carreiras são buscadas em paralelo e a falha de uma não impede as outras; quando a
busca falha, a tela avisa e o resultado do teste continua sendo exibido normalmente.

## Cota e cache

A cota gratuita é de **10.000 unidades por dia** e cada busca (`search.list`) custa **100 unidades**,
ou seja, cerca de 100 buscas por dia. Para economizar, o `VideoService` aceita apenas as carreiras
cadastradas e guarda o resultado de cada uma em cache em memória por **24 horas**
(`app.youtube.cache-hours`). Como existem 49 carreiras, o consumo máximo é de 49 buscas
(4.900 unidades) por dia, qualquer que seja o número de usuários; um termo de busca livre poderia
esgotar a cota com cerca de 100 requisições.

## Configuração

| Variável | Onde | Descrição |
| --- | --- | --- |
| `YOUTUBE_API_KEY` | `backend/.env` (local) e `/opt/bussola/.env` (EC2) | chave da API; nunca versionada |

Propriedades em `application.properties`: `app.youtube.base-url`, `app.youtube.max-results` e
`app.youtube.cache-hours`.

## Segurança e LGPD

- A chave fica apenas no servidor (variável de ambiente) e é restrita, no Google Cloud, à YouTube Data API v3.
- **Nenhum dado pessoal é enviado ao Google**: a requisição leva só o nome da carreira.
- Os vídeos são exibidos como links (miniatura + título), sem player incorporado: o YouTube só recebe
  dados do usuário se ele clicar para assistir. Isso está descrito na Política de Privacidade.
- A busca de vídeos é uma consulta rotineira e não é registrada na auditoria (RN49).

## Testes

- `YouTubeClientTest`: simula a resposta real da API (`MockRestServiceServer`) e valida o mapeamento e
  o tratamento de erro HTTP.
- `VideoServiceTest`: cache por carreira, carreira não cadastrada, chave ausente, falha da API e
  parâmetro em branco.
- `AccessControlIntegrationTest`: endpoint exige login e responde `502`/`400` nos cenários de erro,
  inclusive para carreira não cadastrada.
