package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import br.com.bussolaacademica.integration.youtube.YouTubeClient;
import br.com.bussolaacademica.model.AuditAction;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Busca vídeos de apoio sobre uma área/carreira na YouTube Data API v3.
 *
 * Cada busca custa 100 unidades da cota gratuita (10.000/dia), então os resultados ficam
 * em cache em memória por algumas horas: a mesma área não gasta cota de novo nesse período.
 * Nenhum dado pessoal é enviado ao YouTube, só o nome da área.
 */
@Service
public class VideoService {

    private static final int MAX_AREA_LENGTH = 100;
    private static final String QUERY_SUFFIX = " carreira profissão";

    private final YouTubeClient client;
    private final YouTubeProperties properties;
    private final AuditService auditService;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public VideoService(YouTubeClient client, YouTubeProperties properties, AuditService auditService, Clock clock) {
        this.client = client;
        this.properties = properties;
        this.auditService = auditService;
        this.clock = clock;
    }

    public List<VideoResponse> searchByArea(String area) {
        String normalizedArea = validate(area);
        if (!properties.isConfigured()) {
            throw new ExternalServiceException("A integração com o YouTube não está configurada (YOUTUBE_API_KEY).");
        }
        String key = normalizedArea.toLowerCase(Locale.ROOT);
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(clock.instant())) {
            auditService.recordForCurrentUser(AuditAction.VIDEOS_SEARCHED, "area=" + normalizedArea + " (cache)", true);
            return cached.videos();
        }
        try {
            List<VideoResponse> videos = client.searchVideos(normalizedArea + QUERY_SUFFIX);
            cache.put(key, new CacheEntry(videos, clock.instant().plus(Duration.ofHours(properties.cacheHours()))));
            auditService.recordForCurrentUser(AuditAction.VIDEOS_SEARCHED,
                    "area=" + normalizedArea + " (YouTube, " + videos.size() + " vídeos)", true);
            return videos;
        } catch (ExternalServiceException ex) {
            auditService.recordForCurrentUser(AuditAction.VIDEOS_SEARCHED, "area=" + normalizedArea + " (falha na API)", false);
            throw ex;
        }
    }

    private static String validate(String area) {
        if (area == null || area.isBlank()) {
            throw new IllegalArgumentException("Informe a área para buscar vídeos.");
        }
        String trimmed = area.trim();
        if (trimmed.length() > MAX_AREA_LENGTH) {
            throw new IllegalArgumentException("Área muito longa.");
        }
        return trimmed;
    }

    private record CacheEntry(List<VideoResponse> videos, Instant expiresAt) {
    }
}
