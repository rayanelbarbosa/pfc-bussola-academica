package br.com.bussolaacademica.integration.youtube;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Parte da resposta do endpoint search.list da YouTube Data API v3 que o sistema usa.
 * Campos não mapeados são ignorados.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YouTubeSearchResponse(List<Item> items) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(ItemId id, Snippet snippet) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ItemId(String videoId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Snippet(String title, String channelTitle, Thumbnails thumbnails) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnails(Thumbnail medium, @JsonProperty("default") Thumbnail defaultThumbnail) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Thumbnail(String url) {
    }
}
