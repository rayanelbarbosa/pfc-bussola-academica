package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import br.com.bussolaacademica.integration.youtube.VideoCandidate;
import br.com.bussolaacademica.integration.youtube.YouTubeClient;
import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.repository.CareerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VideoServiceTest {

    private static final YouTubeProperties CONFIGURED =
            new YouTubeProperties("chave-teste", "https://www.googleapis.com/youtube/v3", 15, 6);

    @Mock
    private YouTubeClient client;

    @Mock
    private CareerRepository careerRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T12:00:00Z"), ZoneOffset.UTC);

    private VideoService service(YouTubeProperties properties) {
        return new VideoService(client, careerRepository, properties, new VideoRanker(), clock);
    }

    private void registered(String name) {
        Career career = new Career(1, name, InterestArea.HEALTH, RiasecCategory.INVESTIGATIVE,
                RiasecCategory.SOCIAL, "descrição");
        when(careerRepository.findFirstByNameIgnoreCaseAndActiveTrue(anyString())).thenReturn(Optional.of(career));
    }

    private static VideoCandidate candidate(String id, String title, String description) {
        return new VideoCandidate(new VideoResponse(id, title, "Canal", null, "https://www.youtube.com/watch?v=" + id),
                description, 10_000, 500, 600);
    }

    @Test
    void shouldSearchOnceCacheTheCandidatesAndReturnTheTopThree() {
        registered("Medicina");
        List<VideoCandidate> pool = IntStream.rangeClosed(1, 6)
                .mapToObj(i -> candidate("v" + i, "Medicina profissão " + i, "rotina"))
                .toList();
        when(client.searchCandidates("Medicina carreira profissão")).thenReturn(pool);
        VideoService service = service(CONFIGURED);

        assertThat(service.searchByCareer("Medicina", List.of())).hasSize(VideoService.VIDEOS_PER_CAREER);
        assertThat(service.searchByCareer(" medicina ", List.of(RiasecCategory.SOCIAL))).hasSize(3);

        verify(client, times(1)).searchCandidates(anyString());
    }

    @Test
    void shouldRerankTheCachedCandidatesForEachProfileWithoutCallingYouTubeAgain() {
        registered("Medicina");
        when(client.searchCandidates(anyString())).thenReturn(List.of(
                candidate("pesquisa", "Medicina e pesquisa no laboratório", "ciência, estudo e análise"),
                candidate("pacientes", "Medicina: cuidar de pessoas", "ajudar pacientes no atendimento")));
        VideoService service = service(CONFIGURED);

        assertThat(service.searchByCareer("Medicina", List.of(RiasecCategory.INVESTIGATIVE)).get(0).videoId())
                .isEqualTo("pesquisa");
        assertThat(service.searchByCareer("Medicina", List.of(RiasecCategory.SOCIAL)).get(0).videoId())
                .isEqualTo("pacientes");
        verify(client, times(1)).searchCandidates(anyString());
    }

    @Test
    void shouldRejectUnregisteredCareerWithoutCallingYouTube() {
        when(careerRepository.findFirstByNameIgnoreCaseAndActiveTrue("Astronauta")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service(CONFIGURED).searchByCareer("Astronauta", List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Carreira não encontrada");
        verify(client, never()).searchCandidates(anyString());
    }

    @Test
    void shouldFailClearlyWhenApiKeyIsMissing() {
        registered("Direito");
        VideoService service = service(new YouTubeProperties("", "https://www.googleapis.com/youtube/v3", 15, 6));

        assertThatThrownBy(() -> service.searchByCareer("Direito", List.of()))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("YOUTUBE_API_KEY");
        verify(client, never()).searchCandidates(anyString());
    }

    @Test
    void shouldPropagateFailureWhenYouTubeIsDown() {
        registered("Design");
        when(client.searchCandidates(anyString())).thenThrow(new ExternalServiceException("fora do ar"));

        assertThatThrownBy(() -> service(CONFIGURED).searchByCareer("Design", List.of()))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldRejectBlankCareer() {
        assertThatThrownBy(() -> service(CONFIGURED).searchByCareer("  ", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        verify(careerRepository, never()).findFirstByNameIgnoreCaseAndActiveTrue(anyString());
    }
}
