package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
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
            new YouTubeProperties("chave-teste", "https://www.googleapis.com/youtube/v3", 3, 6);
    private static final VideoResponse VIDEO = new VideoResponse("abc123", "Como é cursar Medicina",
            "Canal", "https://i.ytimg.com/vi/abc123/mqdefault.jpg", "https://www.youtube.com/watch?v=abc123");

    @Mock
    private YouTubeClient client;

    @Mock
    private CareerRepository careerRepository;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T12:00:00Z"), ZoneOffset.UTC);

    private VideoService service(YouTubeProperties properties) {
        return new VideoService(client, careerRepository, properties, clock);
    }

    private void registered(String name) {
        Career career = new Career(1, name, InterestArea.HEALTH, RiasecCategory.INVESTIGATIVE,
                RiasecCategory.SOCIAL, "descrição");
        when(careerRepository.findFirstByNameIgnoreCase(anyString())).thenReturn(Optional.of(career));
    }

    @Test
    void shouldSearchOnYouTubeAndCacheByCareer() {
        registered("Medicina");
        when(client.searchVideos("Medicina carreira profissão")).thenReturn(List.of(VIDEO));
        VideoService service = service(CONFIGURED);

        assertThat(service.searchByCareer("Medicina")).containsExactly(VIDEO);
        assertThat(service.searchByCareer(" medicina ")).containsExactly(VIDEO);

        verify(client, times(1)).searchVideos(anyString());
    }

    @Test
    void shouldRejectUnregisteredCareerWithoutCallingYouTube() {
        when(careerRepository.findFirstByNameIgnoreCase("Astronauta")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service(CONFIGURED).searchByCareer("Astronauta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Carreira não encontrada");
        verify(client, never()).searchVideos(anyString());
    }

    @Test
    void shouldFailClearlyWhenApiKeyIsMissing() {
        registered("Direito");
        VideoService service = service(new YouTubeProperties("", "https://www.googleapis.com/youtube/v3", 3, 6));

        assertThatThrownBy(() -> service.searchByCareer("Direito"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("YOUTUBE_API_KEY");
        verify(client, never()).searchVideos(anyString());
    }

    @Test
    void shouldPropagateFailureWhenYouTubeIsDown() {
        registered("Design");
        when(client.searchVideos(anyString())).thenThrow(new ExternalServiceException("fora do ar"));

        assertThatThrownBy(() -> service(CONFIGURED).searchByCareer("Design"))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldRejectBlankCareer() {
        assertThatThrownBy(() -> service(CONFIGURED).searchByCareer("  "))
                .isInstanceOf(IllegalArgumentException.class);
        verify(careerRepository, never()).findFirstByNameIgnoreCase(anyString());
    }
}
