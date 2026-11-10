package br.com.bussolaacademica.integration.youtube;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Cliente HTTP da YouTube Data API v3.
 * <ol>
 *   <li>GET /search (100 unidades da cota): os candidatos para a busca, com título e descrição.</li>
 *   <li>GET /videos (1 unidade): visualizações, curtidas e duração de todos os candidatos de uma vez.</li>
 * </ol>
 * Só este componente conhece o formato da API externa; o resto do sistema usa {@link VideoCandidate}.
 */
@Component
public class YouTubeClient {

    private static final String WATCH_URL = "https://www.youtube.com/watch?v=";

    private final RestClient restClient;
    private final YouTubeProperties properties;

    public YouTubeClient(RestClient.Builder builder, YouTubeProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    public List<VideoCandidate> searchCandidates(String query) {
        try {
            YouTubeSearchResponse response = restClient.get()
                    .uri(uri -> uri.path("/search")
                            .queryParam("part", "snippet")
                            .queryParam("type", "video")
                            .queryParam("maxResults", properties.maxResults())
                            .queryParam("q", query)
                            .queryParam("regionCode", "BR")
                            .queryParam("relevanceLanguage", "pt")
                            .queryParam("safeSearch", "strict")
                            .queryParam("key", properties.apiKey())
                            .build())
                    .retrieve()
                    .body(YouTubeSearchResponse.class);
            if (response == null || response.items() == null) {
                return List.of();
            }
            List<YouTubeSearchResponse.Item> items = response.items().stream()
                    .filter(item -> item.id() != null && item.id().videoId() != null && item.snippet() != null)
                    .toList();
            if (items.isEmpty()) {
                return List.of();
            }
            Map<String, YouTubeVideosResponse.Item> details = fetchDetails(items);
            return items.stream()
                    .map(item -> toCandidate(item, details.get(item.id().videoId())))
                    .toList();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Não foi possível buscar vídeos no YouTube agora.", ex);
        }
    }

    /** Uma única chamada para todos os ids (a API aceita até 50 por vez). */
    private Map<String, YouTubeVideosResponse.Item> fetchDetails(List<YouTubeSearchResponse.Item> items) {
        String ids = items.stream().map(item -> item.id().videoId()).collect(Collectors.joining(","));
        YouTubeVideosResponse response = restClient.get()
                .uri(uri -> uri.path("/videos")
                        .queryParam("part", "statistics,contentDetails")
                        .queryParam("id", ids)
                        .queryParam("key", properties.apiKey())
                        .build())
                .retrieve()
                .body(YouTubeVideosResponse.class);
        if (response == null || response.items() == null) {
            return Map.of();
        }
        return response.items().stream()
                .filter(item -> item.id() != null)
                .collect(Collectors.toMap(YouTubeVideosResponse.Item::id, Function.identity(), (first, second) -> first));
    }

    private static VideoCandidate toCandidate(YouTubeSearchResponse.Item item, YouTubeVideosResponse.Item details) {
        YouTubeSearchResponse.Snippet snippet = item.snippet();
        String thumbnail = null;
        if (snippet.thumbnails() != null) {
            YouTubeSearchResponse.Thumbnail chosen = snippet.thumbnails().medium() != null
                    ? snippet.thumbnails().medium() : snippet.thumbnails().defaultThumbnail();
            thumbnail = chosen == null ? null : chosen.url();
        }
        // A API devolve títulos com entidades HTML (ex.: &quot;), por isso o unescape
        VideoResponse video = new VideoResponse(item.id().videoId(), HtmlUtils.htmlUnescape(snippet.title()),
                snippet.channelTitle(), thumbnail, WATCH_URL + item.id().videoId());
        String description = snippet.description() == null ? "" : HtmlUtils.htmlUnescape(snippet.description());
        long views = 0;
        long likes = 0;
        long seconds = 0;
        if (details != null) {
            if (details.statistics() != null) {
                views = parseCount(details.statistics().viewCount());
                likes = parseCount(details.statistics().likeCount());
            }
            if (details.contentDetails() != null) {
                seconds = parseDuration(details.contentDetails().duration());
            }
        }
        return new VideoCandidate(video, description, views, likes, seconds);
    }

    /** Contador ausente (ex.: curtidas ocultas pelo canal) ou inválido vale 0. */
    static long parseCount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    /** Duração ISO 8601 em segundos ("PT8M15S" = 495); ausente ou inválida vale 0 (desconhecida). */
    static long parseDuration(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Duration.parse(value.trim()).getSeconds();
        } catch (DateTimeParseException ex) {
            return 0;
        }
    }
}
