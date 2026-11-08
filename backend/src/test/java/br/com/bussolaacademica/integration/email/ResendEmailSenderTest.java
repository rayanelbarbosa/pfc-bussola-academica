package br.com.bussolaacademica.integration.email;

import br.com.bussolaacademica.config.MailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.ExpectedCount.never;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Testa o cliente da Resend contra o formato real da API (POST /emails com Bearer). */
class ResendEmailSenderTest {

    private static final String BASE_URL = "https://api.resend.com";

    private ResendEmailSender sender(String apiKey, MockRestServiceServer[] server) {
        RestClient.Builder builder = RestClient.builder();
        server[0] = MockRestServiceServer.bindTo(builder).build();
        return new ResendEmailSender(builder, new MailProperties(apiKey, BASE_URL,
                "Bussola <nao-responda@mail.teste.com>", "http://localhost:4200"));
    }

    @Test
    void shouldPostEmailToResendWithApiKey() {
        MockRestServiceServer[] server = new MockRestServiceServer[1];
        ResendEmailSender sender = sender("re_teste", server);
        server[0].expect(requestTo(BASE_URL + "/emails"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer re_teste"))
                .andExpect(content().string(containsString("\"to\":[\"aluna@teste.com\"]")))
                .andExpect(content().string(containsString("\"subject\":\"Assunto\"")))
                .andRespond(withSuccess("{\"id\":\"email-1\"}", org.springframework.http.MediaType.APPLICATION_JSON));

        sender.send("aluna@teste.com", "Assunto", "<p>Oi</p>", "Oi");

        server[0].verify();
    }

    @Test
    void shouldNotCallTheApiWithoutKey() {
        MockRestServiceServer[] server = new MockRestServiceServer[1];
        ResendEmailSender sender = sender("", server);
        server[0].expect(never(), requestTo(BASE_URL + "/emails"));

        sender.send("aluna@teste.com", "Assunto", "<p>Oi</p>", "Oi");

        server[0].verify();
    }

    @Test
    void shouldSwallowProviderErrorsSoTheUserIsNotAffected() {
        MockRestServiceServer[] server = new MockRestServiceServer[1];
        ResendEmailSender sender = sender("re_teste", server);
        server[0].expect(requestTo(BASE_URL + "/emails")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        sender.send("aluna@teste.com", "Assunto", "<p>Oi</p>", "Oi");

        server[0].verify();
    }
}
