package br.com.bussolaacademica.integration.email;

import br.com.bussolaacademica.config.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

/**
 * Cliente da API da Resend (POST /emails).
 *
 * O envio é assíncrono (@Async): a resposta de "esqueci minha senha" sai na hora, com o mesmo tempo
 * para e-mail cadastrado ou não, e uma falha no provedor não aparece para quem pediu. A falha fica no log,
 * sem o conteúdo da mensagem (que tem o link de redefinição).
 */
@Component
public class ResendEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);

    private final RestClient restClient;
    private final MailProperties properties;

    public ResendEmailSender(RestClient.Builder builder, MailProperties properties) {
        this.restClient = builder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    @Async
    @Override
    public void send(String to, String subject, String html, String text) {
        if (!properties.isConfigured()) {
            log.warn("E-mail não enviado: RESEND_API_KEY não configurada");
            return;
        }
        try {
            restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.resendApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("from", properties.from(), "to", List.of(to), "subject", subject,
                            "html", html, "text", text))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Falha ao enviar e-mail pela Resend: {}", ex.getMessage());
        }
    }
}
