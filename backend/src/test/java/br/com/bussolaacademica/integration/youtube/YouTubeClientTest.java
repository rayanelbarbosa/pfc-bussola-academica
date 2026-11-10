package br.com.bussolaacademica.integration.youtube;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Testa o cliente contra respostas simuladas no formato real da YouTube Data API v3 (search e videos). */
class YouTubeClientTest {

    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";

    private MockRestServiceServer server;
    private YouTubeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new YouTubeClient(builder, new YouTubeProperties("chave-teste", BASE_URL, 15, 6));
    }

    @Test
    void shouldJoinSearchResultsWithStatistics() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andExpect(queryParam("type", "video"))
                .andExpect(queryParam("maxResults", "15"))
                .andExpect(queryParam("key", "chave-teste"))
                .andRespond(withSuccess("""
                        {
                          "kind": "youtube#searchListResponse",
                          "items": [
                            {
                              "id": { "kind": "youtube#video", "videoId": "abc123" },
                              "snippet": {
                                "title": "Um dia na faculdade de &quot;Medicina&quot;",
                                "description": "Rotina de quem cuida de pacientes",
                                "channelTitle": "Canal Carreiras",
                                "thumbnails": { "medium": { "url": "https://i.ytimg.com/vi/abc123/mqdefault.jpg" } }
                              }
                            },
                            {
                              "id": { "kind": "youtube#video", "videoId": "sem-estatistica" },
                              "snippet": { "title": "Medicina", "channelTitle": "Outro canal" }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo(startsWith(BASE_URL + "/videos")))
                .andExpect(queryParam("part", "statistics,contentDetails"))
                .andRespond(withSuccess("""
                        {
                          "items": [
                            {
                              "id": "abc123",
                              "statistics": { "viewCount": "120500", "likeCount": "4300" },
                              "contentDetails": { "duration": "PT8M15S" }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<VideoCandidate> candidates = client.searchCandidates("Medicina carreira profissão");

        assertThat(candidates).hasSize(2);
        VideoCandidate first = candidates.get(0);
        assertThat(first.video().title()).isEqualTo("Um dia na faculdade de \"Medicina\"");
        assertThat(first.video().url()).isEqualTo("https://www.youtube.com/watch?v=abc123");
        assertThat(first.video().thumbnailUrl()).endsWith("mqdefault.jpg");
        assertThat(first.description()).isEqualTo("Rotina de quem cuida de pacientes");
        assertThat(first.views()).isEqualTo(120500);
        assertThat(first.likes()).isEqualTo(4300);
        assertThat(first.durationSeconds()).isEqualTo(495);
        // vídeo sem estatísticas na resposta: contadores zerados, sem quebrar a busca
        assertThat(candidates.get(1).views()).isZero();
        assertThat(candidates.get(1).description()).isEmpty();
        server.verify();
    }

    @Test
    void shouldNotCallVideosEndpointWhenSearchIsEmpty() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andRespond(withSuccess("{\"items\": []}", MediaType.APPLICATION_JSON));

        assertThat(client.searchCandidates("Carreira rara")).isEmpty();
        server.verify();
    }

    @Test
    void shouldTranslateApiErrorsIntoExternalServiceException() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.searchCandidates("Direito"))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void shouldParseCountsAndDurationsDefensively() {
        assertThat(YouTubeClient.parseCount("42")).isEqualTo(42);
        assertThat(YouTubeClient.parseCount(null)).isZero();
        assertThat(YouTubeClient.parseCount("muitos")).isZero();
        assertThat(YouTubeClient.parseDuration("PT1H2M3S")).isEqualTo(3723);
        assertThat(YouTubeClient.parseDuration("P0D")).isZero();
        assertThat(YouTubeClient.parseDuration("invalida")).isZero();
        assertThat(YouTubeClient.parseDuration(" ")).isZero();
    }
}
