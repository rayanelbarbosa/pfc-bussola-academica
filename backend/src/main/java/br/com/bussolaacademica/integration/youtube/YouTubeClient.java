package br.com.bussolaacademica.integration.youtube;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * Cliente HTTP da YouTube Data API v3 (endpoint GET /search).
 * Só este componente conhece o formato da API externa; o resto do sistema usa {@link VideoResponse}.
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

    public List<VideoResponse> searchVideos(String query) {
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
            return response.items().stream()
                    .filter(item -> item.id() != null && item.id().videoId() != null && item.snippet() != null)
                    .map(YouTubeClient::toVideo)
                    .toList();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("Não foi possível buscar vídeos no YouTube agora.", ex);
        }
    }

    private static VideoResponse toVideo(YouTubeSearchResponse.Item item) {
        YouTubeSearchResponse.Snippet snippet = item.snippet();
        String thumbnail = null;
        if (snippet.thumbnails() != null) {
            YouTubeSearchResponse.Thumbnail chosen = snippet.thumbnails().medium() != null
                    ? snippet.thumbnails().medium() : snippet.thumbnails().defaultThumbnail();
            thumbnail = chosen == null ? null : chosen.url();
        }
        // A API devolve títulos com entidades HTML (ex.: &quot;), por isso o unescape
        return new VideoResponse(item.id().videoId(), HtmlUtils.htmlUnescape(snippet.title()),
                snippet.channelTitle(), thumbnail, WATCH_URL + item.id().videoId());
    }
}
