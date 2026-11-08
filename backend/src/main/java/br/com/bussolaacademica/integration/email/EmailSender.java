package br.com.bussolaacademica.integration.email;

/** Envio de e-mail transacional. A implementação real usa a API da Resend. */
public interface EmailSender {

    void send(String to, String subject, String html, String text);
}
