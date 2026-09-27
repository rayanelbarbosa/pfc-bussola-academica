package br.com.bussolaacademica.integration.youtube;

import br.com.bussolaacademica.config.YouTubeProperties;
import br.com.bussolaacademica.dto.VideoResponse;
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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.startsWith;

/** Testa o cliente contra uma resposta simulada no formato real da YouTube Data API v3. */
class YouTubeClientTest {

    private static final String BASE_URL = "https://www.googleapis.com/youtube/v3";

    private MockRestServiceServer server;
    private YouTubeClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new YouTubeClient(builder, new YouTubeProperties("chave-teste", BASE_URL, 3, 6));
    }

    @Test
    void shouldMapSearchResponseToVideos() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andExpect(queryParam("type", "video"))
                .andExpect(queryParam("key", "chave-teste"))
                .andRespond(withSuccess("""
                        {
                          "kind": "youtube#searchListResponse",
                          "items": [
                            {
                              "id": { "kind": "youtube#video", "videoId": "abc123" },
                              "snippet": {
                                "title": "Um dia na faculdade de &quot;Medicina&quot;",
                                "channelTitle": "Canal Carreiras",
                                "thumbnails": { "medium": { "url": "https://i.ytimg.com/vi/abc123/mqdefault.jpg" } }
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<VideoResponse> videos = client.searchVideos("Medicina carreira profissão");

        assertThat(videos).hasSize(1);
        assertThat(videos.get(0).title()).isEqualTo("Um dia na faculdade de \"Medicina\"");
        assertThat(videos.get(0).url()).isEqualTo("https://www.youtube.com/watch?v=abc123");
        assertThat(videos.get(0).thumbnailUrl()).endsWith("mqdefault.jpg");
        server.verify();
    }

    @Test
    void shouldTranslateApiErrorsIntoExternalServiceException() {
        server.expect(requestTo(startsWith(BASE_URL + "/search")))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> client.searchVideos("Direito"))
                .isInstanceOf(ExternalServiceException.class);
    }
}
