package br.com.bussolaacademica.integration.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Parte da resposta do endpoint videos.list (part=statistics,contentDetails) da YouTube Data API v3.
 * A API manda os contadores como texto e a duração no formato ISO 8601 (ex.: "PT8M15S").
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubeVideosResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(String id, Statistics statistics, ContentDetails contentDetails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Statistics(String viewCount, String likeCount) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentDetails(String duration) {
    }
}
