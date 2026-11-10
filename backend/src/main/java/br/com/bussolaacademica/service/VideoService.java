package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import br.com.bussolaacademica.integration.youtube.VideoCandidate;
import br.com.bussolaacademica.integration.youtube.YouTubeClient;
import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.repository.CareerRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Busca e ordena vídeos de apoio sobre uma carreira cadastrada (YouTube Data API v3).
 *
 * <p>Cada carreira custa 101 unidades da cota gratuita de 10.000/dia (busca = 100, estatísticas = 1). Por isso
 * só são aceitas carreiras cadastradas e ativas, e os candidatos ficam em cache em memória (24 h por padrão):
 * o consumo máximo é de uma busca por carreira por dia, qualquer que seja o número de usuários.</p>
 *
 * <p>O cache guarda os candidatos, não a ordem final: o {@link VideoRanker} reordena para cada estudante, de
 * acordo com o perfil RIASEC dele, sem gastar cota. Nenhum dado pessoal é enviado ao YouTube, só o nome da
 * carreira.</p>
 */
@Service
public class VideoService {

    static final int VIDEOS_PER_CAREER = 3;
    private static final String QUERY_SUFFIX = " carreira profissão";

    private final YouTubeClient client;
    private final CareerRepository careerRepository;
    private final YouTubeProperties properties;
    private final VideoRanker ranker;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public VideoService(YouTubeClient client, CareerRepository careerRepository, YouTubeProperties properties,
                        VideoRanker ranker, Clock clock) {
        this.client = client;
        this.careerRepository = careerRepository;
        this.properties = properties;
        this.ranker = ranker;
        this.clock = clock;
    }

    public List<VideoResponse> searchByCareer(String careerName, List<RiasecCategory> profile) {
        String name = findRegisteredCareer(careerName);
        if (!properties.isConfigured()) {
            throw new ExternalServiceException("A integração com o YouTube não está configurada (YOUTUBE_API_KEY).");
        }
        return ranker.rank(candidates(name), name, profile, VIDEOS_PER_CAREER);
    }

    private List<VideoCandidate> candidates(String name) {
        CacheEntry cached = cache.get(name);
        if (cached != null && cached.expiresAt().isAfter(clock.instant())) {
            return cached.candidates();
        }
        List<VideoCandidate> candidates = client.searchCandidates(name + QUERY_SUFFIX);
        cache.put(name, new CacheEntry(candidates, clock.instant().plus(Duration.ofHours(properties.cacheHours()))));
        return candidates;
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

    private record CacheEntry(List<VideoCandidate> candidates, Instant expiresAt) {
    }
}
