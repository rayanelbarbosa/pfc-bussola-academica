package br.com.bussolaacademica.service;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.exception.ExternalServiceException;
import br.com.bussolaacademica.integration.youtube.YouTubeClient;
import br.com.bussolaacademica.model.AuditAction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
    private AuditService auditService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T12:00:00Z"), ZoneOffset.UTC);

    @Test
    void shouldSearchOnYouTubeAndCacheByArea() {
        VideoService service = new VideoService(client, CONFIGURED, auditService, clock);
        when(client.searchVideos("Medicina carreira profissão")).thenReturn(List.of(VIDEO));

        assertThat(service.searchByArea("Medicina")).containsExactly(VIDEO);
        assertThat(service.searchByArea(" medicina ")).containsExactly(VIDEO);

        verify(client, times(1)).searchVideos(anyString());
        verify(auditService, times(2)).recordForCurrentUser(eq(AuditAction.VIDEOS_SEARCHED), anyString(), eq(true));
    }

    @Test
    void shouldFailClearlyWhenApiKeyIsMissing() {
        VideoService service = new VideoService(client,
                new YouTubeProperties("", "https://www.googleapis.com/youtube/v3", 3, 6), auditService, clock);

        assertThatThrownBy(() -> service.searchByArea("Direito"))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("YOUTUBE_API_KEY");
        verify(client, never()).searchVideos(anyString());
    }

    @Test
    void shouldAuditFailureWhenYouTubeIsDown() {
        VideoService service = new VideoService(client, CONFIGURED, auditService, clock);
        when(client.searchVideos(anyString())).thenThrow(new ExternalServiceException("fora do ar"));

        assertThatThrownBy(() -> service.searchByArea("Design")).isInstanceOf(ExternalServiceException.class);
        verify(auditService).recordForCurrentUser(eq(AuditAction.VIDEOS_SEARCHED), anyString(), eq(false));
    }

    @Test
    void shouldRejectBlankArea() {
        VideoService service = new VideoService(client, CONFIGURED, auditService, clock);

        assertThatThrownBy(() -> service.searchByArea("  ")).isInstanceOf(IllegalArgumentException.class);
    }
}
