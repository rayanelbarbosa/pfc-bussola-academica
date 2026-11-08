package br.com.bussolaacademica.integration;

import br.com.bussolaacademica.integration.email.EmailSender;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.PasswordResetToken;
import br.com.bussolaacademica.repository.AuditLogRepository;
import br.com.bussolaacademica.repository.PasswordResetTokenRepository;
import br.com.bussolaacademica.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.bussolaacademica.support.AuthTestHelper.DEFAULT_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.loginJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Recuperação de senha (RF04): mensagem neutra, token de uso único com validade e fim das sessões abertas. */
@SpringBootTest
@AutoConfigureMockMvc
class PasswordResetIntegrationTest {

    private static final Pattern TOKEN = Pattern.compile("redefinir-senha\\?token=([A-Za-z0-9_-]+)");
    private static final String NEW_PASSWORD = "NovaSenha2026";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private Clock clock;

    @MockBean
    private EmailSender emailSender;

    private void forgot(String email) throws Exception {
        mockMvc.perform(post("/api/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message", containsString("Se o e-mail estiver cadastrado")));
    }

    /** Pega o token do link enviado no e-mail (texto puro). */
    private String sentToken(String email) {
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq(email), anyString(), anyString(), text.capture());
        Matcher matcher = TOKEN.matcher(text.getValue());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private String resetJson(String token, String password) {
        return "{\"token\":\"" + token + "\",\"newPassword\":\"" + password + "\"}";
    }

    @Test
    void shouldResetPasswordWithTheEmailedLinkAndCloseOpenSessions() throws Exception {
        String email = uniqueEmail();
        String oldSession = registerStudent(mockMvc, email);
        Thread.sleep(1100); // o token antigo precisa ser de um segundo anterior à troca

        forgot(email);
        String token = sentToken(email);
        assertThat(tokenRepository.findAll()).noneMatch(saved -> saved.getTokenHash().equals(token));

        mockMvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson(token, NEW_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(oldSession)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, DEFAULT_PASSWORD)))
                .andExpect(status().isUnauthorized());
        String newSession = login(mockMvc, email, NEW_PASSWORD);
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(newSession)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotAcceptTheSameLinkTwice() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);
        forgot(email);
        String token = sentToken(email);

        mockMvc.perform(post("/api/auth/password/reset/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson(token, NEW_PASSWORD)))
                .andExpect(status().isNoContent());

        // Ao abrir o link de novo, a tela já sabe que ele não vale mais
        mockMvc.perform(post("/api/auth/password/reset/validate").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + token + "\"}"))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson(token, "OutraSenha99")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("inválido ou expirou")));
    }

    @Test
    void shouldRejectExpiredLink() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);
        forgot(email);
        String token = sentToken(email);
        Long userId = userRepository.findByEmailIgnoreCase(email).orElseThrow().getId();
        PasswordResetToken saved = tokenRepository.findAll().stream()
                .filter(candidate -> candidate.getUserId().equals(userId)).findFirst().orElseThrow();
        ReflectionTestUtils.setField(saved, "expiresAt", LocalDateTime.now(clock).minusMinutes(1));
        tokenRepository.save(saved);

        mockMvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson(token, NEW_PASSWORD)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldGiveTheSameAnswerForUnknownEmailWithoutSendingAnything() throws Exception {
        String email = uniqueEmail();

        forgot(email);

        verify(emailSender, never()).send(anyString(), anyString(), anyString(), anyString());
        assertThat(auditLogRepository.findAll()).anyMatch(log ->
                log.getAction() == AuditAction.PASSWORD_RESET_REQUESTED && email.equals(log.getUserEmail()));
    }

    @Test
    void shouldSendAtMostOneEmailEveryTwoMinutes() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);
        forgot(email);
        sentToken(email);
        clearInvocations(emailSender);

        forgot(email);

        verify(emailSender, never()).send(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldRejectInvalidTokenAndWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson("token-que-nao-existe", NEW_PASSWORD)))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                        .content(resetJson("qualquer", "fraca")))
                .andExpect(status().isBadRequest());
    }
}
