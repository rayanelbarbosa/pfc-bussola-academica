package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import br.com.bussolaacademica.integration.youtube.YouTubeClient;
import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.repository.CareerRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Busca vídeos de apoio sobre uma carreira cadastrada na YouTube Data API v3.
 *
 * Cada busca custa 100 unidades da cota gratuita (10.000/dia). Por isso só são aceitas carreiras
 * que existem na tabela career, e o resultado fica em cache em memória (24 h por padrão): o consumo
 * máximo é de uma busca por carreira cadastrada por dia, qualquer que seja o número de usuários.
 * Nenhum dado pessoal é enviado ao YouTube, só o nome da carreira.
 */
@Service
public class VideoService {

    private static final String QUERY_SUFFIX = " carreira profissão";

    private final YouTubeClient client;
    private final CareerRepository careerRepository;
    private final YouTubeProperties properties;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public VideoService(YouTubeClient client, CareerRepository careerRepository, YouTubeProperties properties, Clock clock) {
        this.client = client;
        this.careerRepository = careerRepository;
        this.properties = properties;
        this.clock = clock;
    }

    public List<VideoResponse> searchByCareer(String careerName) {
        String name = findRegisteredCareer(careerName);
        if (!properties.isConfigured()) {
            throw new ExternalServiceException("A integração com o YouTube não está configurada (YOUTUBE_API_KEY).");
        }
        CacheEntry cached = cache.get(name);
        if (cached != null && cached.expiresAt().isAfter(clock.instant())) {
            return cached.videos();
        }
        List<VideoResponse> videos = client.searchVideos(name + QUERY_SUFFIX);
        cache.put(name, new CacheEntry(videos, clock.instant().plus(Duration.ofHours(properties.cacheHours()))));
        return videos;
    }

    /** Aceita só carreiras cadastradas e ativas e devolve o nome oficial (usado na busca e no cache). */
    private String findRegisteredCareer(String careerName) {
        if (careerName == null || careerName.isBlank()) {
            throw new IllegalArgumentException("Informe a carreira para buscar vídeos.");
        }
        return careerRepository.findFirstByNameIgnoreCaseAndActiveTrue(careerName.trim())
                .map(Career::getName)
                .orElseThrow(() -> new IllegalArgumentException("Carreira não encontrada."));
    }

    private record CacheEntry(List<VideoResponse> videos, Instant expiresAt) {
    }
}
