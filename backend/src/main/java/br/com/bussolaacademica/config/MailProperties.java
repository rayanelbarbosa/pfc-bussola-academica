package br.com.bussolaacademica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Envio de e-mail transacional pela Resend (app.mail.*).
 * A chave vem de RESEND_API_KEY; o remetente precisa ser do domínio verificado na Resend.
 * frontendUrl é o endereço do site usado nos links enviados por e-mail.
 */
@ConfigurationProperties(prefix = "app.mail")
public record MailProperties(String resendApiKey, String baseUrl, String from, String frontendUrl) {

    public boolean isConfigured() {
        return resendApiKey != null && !resendApiKey.isBlank();
    }
}
