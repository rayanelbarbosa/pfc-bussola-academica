package br.com.bussolaacademica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Integração com a YouTube Data API v3 (app.youtube.*). A chave vem de YOUTUBE_API_KEY. */
@ConfigurationProperties(prefix = "app.youtube")
public record YouTubeProperties(String apiKey, String baseUrl, int maxResults, int cacheHours) {

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
