# Integração com API externa — YouTube Data API v3

## Objetivo

Depois do teste vocacional, a tela de resultado oferece vídeos públicos sobre cada carreira (as 4 recomendadas
e qualquer uma do ranking completo). Assim o estudante conhece o dia a dia da profissão antes de escolher o
curso. Os vídeos são carregados **sob demanda**: só quando o estudante clica em "Ver vídeos".

## Visão geral

```
Angular (tela de resultado)
   │  GET /api/videos?career=Psicologia&profile=SOCIAL      (JWT no cabeçalho Authorization)
   ▼
VideoController ──> VideoService ──(cache 24 h dos candidatos)──> YouTubeClient
                        │                                           │  GET /youtube/v3/search  (100 unidades)
                        │                                           │  GET /youtube/v3/videos  (1 unidade)
                        ▼                                           ▼
                    VideoRanker (ordena para o perfil)        YouTube Data API v3
```

- O front-end **nunca** chama o YouTube diretamente: a chave da API fica só no back-end.
- `YouTubeClient` é o único componente que conhece o formato da API externa. Ele converte a resposta para o
  objeto interno `VideoCandidate`, então uma mudança na API do Google afeta só essa classe.

## Chamadas à API externa

**1. Busca dos candidatos** — `GET https://www.googleapis.com/youtube/v3/search`

| Parâmetro | Valor |
| --- | --- |
| `key` | chave de API criada no Google Cloud Console e restrita à YouTube Data API v3 |
| `part` | `snippet` (título, descrição, canal, miniatura) |
| `type` | `video` |
| `q` | `<carreira> carreira profissão` (ex.: `Psicologia carreira profissão`) |
| `maxResults` | `15` candidatos (configurável em `app.youtube.max-results`) |
| `regionCode` / `relevanceLanguage` | `BR` / `pt` |
| `safeSearch` | `strict` (o público inclui adolescentes) |

**2. Estatísticas dos candidatos** — `GET https://www.googleapis.com/youtube/v3/videos`, com
`part=statistics,contentDetails` e os 15 ids de uma vez: visualizações, curtidas e duração.

Os títulos vêm com entidades HTML (ex.: `&quot;`) e são convertidos para texto normal.

## Ranking dos vídeos (`VideoRanker`)

O YouTube ordena só pela relevância da busca. O `VideoRanker` reordena os 15 candidatos para cada estudante e
devolve os **3 melhores**:

| Sinal | Peso | Como é calculado |
| --- | --- | --- |
| Relevância textual | 0,6 | BM25 do título e da descrição contra os termos da carreira, termos de "como é a profissão" e palavras dos tipos RIASEC do perfil do estudante |
| Popularidade | 0,3 | log10 das visualizações, normalizado entre os candidatos (um vídeo viral não apaga os outros) |
| Aprovação | 0,1 | curtidas por visualização |

Regras extras: vídeos com menos de 60 s (shorts) saem; vídeos com mais de 40 min (aulas e lives) e vídeos que
não citam nenhuma palavra do nome da carreira perdem metade da nota.

## Endpoint próprio exposto ao front-end

`GET /api/videos?career={nome da carreira}&profile={tipos RIASEC}`: exige usuário autenticado e aceita apenas
carreiras **cadastradas e ativas** na tabela `career` (sem diferenciar maiúsculas e minúsculas). `profile` é
opcional (ex.: `SOCIAL,INVESTIGATIVE`) e só muda a ordem dos vídeos, nunca a busca no YouTube.

```json
[
  {
    "videoId": "abc123",
    "title": "Como é o dia a dia de um psicólogo",
    "channelTitle": "Canal Carreiras",
    "thumbnailUrl": "https://i.ytimg.com/vi/abc123/mqdefault.jpg",
    "url": "https://www.youtube.com/watch?v=abc123"
  }
]
```

| Situação | Resposta |
| --- | --- |
| Sucesso | `200` com a lista (pode ser vazia) |
| `career` ausente, em branco, não cadastrada ou desativada | `400` (o YouTube não é chamado) |
| Sem token / token expirado | `401` |
| Chave não configurada ou erro/indisponibilidade do YouTube | `502` com mensagem amigável |

No front-end, a falha nos vídeos de uma carreira não afeta as outras nem o resultado do teste: a tela só avisa
que os vídeos não puderam ser carregados.

## Cota e cache

A cota gratuita é de **10.000 unidades por dia**. Cada carreira custa **101 unidades** (busca = 100,
estatísticas = 1). Para economizar, o `VideoService` aceita apenas carreiras cadastradas e guarda os
**candidatos** de cada uma em cache em memória por **24 horas** (`app.youtube.cache-hours`). O cache guarda os
candidatos e não a ordem final, então reordenar para cada perfil não gasta cota. Com as 70 carreiras do
catálogo, o consumo máximo é de 7.070 unidades por dia, qualquer que seja o número de usuários; um termo de
busca livre poderia esgotar a cota com cerca de 100 requisições.

## Configuração

| Variável | Onde | Descrição |
| --- | --- | --- |
| `YOUTUBE_API_KEY` | `backend/.env` (local) e `/opt/bussola/.env` (EC2) | chave da API; nunca versionada |

Propriedades em `application.properties`: `app.youtube.base-url`, `app.youtube.max-results` e
`app.youtube.cache-hours`.

## Segurança e LGPD

- A chave fica apenas no servidor (variável de ambiente) e é restrita, no Google Cloud, à YouTube Data API v3.
- **Nenhum dado pessoal é enviado ao Google**: a requisição leva só o nome da carreira. O perfil RIASEC é usado
  apenas dentro do back-end, para ordenar.
- Os vídeos são exibidos como links (miniatura + título), sem player incorporado: o YouTube só recebe dados do
  usuário se ele clicar para assistir. Isso está descrito na Política de Privacidade.
- A busca de vídeos é uma consulta rotineira e não é registrada na auditoria.

## Testes

- `YouTubeClientTest`: simula as respostas reais da API (`MockRestServiceServer`) e valida o mapeamento das duas
  chamadas e o tratamento de erro HTTP.
- `VideoServiceTest`: cache por carreira, carreira não cadastrada, chave ausente, falha da API e parâmetro em branco.
- `VideoRankerTest`: relevância vence popularidade fora do tema, popularidade desempata, o perfil muda a ordem,
  shorts saem e vídeos muito longos perdem nota, limite de 3 vídeos, normalização do texto e BM25.
- `AccessControlIntegrationTest`: o endpoint exige login e responde `502`/`400` nos cenários de erro, inclusive
  para carreira não cadastrada.
