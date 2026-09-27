package br.com.bussolaacademica.integration;

import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.repository.AuditLogRepository;
import br.com.bussolaacademica.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.bussolaacademica.support.AuthTestHelper.DEFAULT_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.loginJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void shouldRegisterStudentWithEncryptedPasswordAndTermsAcceptance() throws Exception {
        String email = uniqueEmail();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Marina Alves", email, DEFAULT_PASSWORD, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andExpect(jsonPath("$.user.termsVersion").value("1.0"))
                .andExpect(jsonPath("$.user.password").doesNotExist());

        var saved = userRepository.findByEmailIgnoreCase(email).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(DEFAULT_PASSWORD).startsWith("$2");
        assertThat(saved.getTermsAcceptedAt()).isNotNull();
    }

    @Test
    void shouldRejectRegistrationWithoutAcceptingTerms() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Sem Aceite", uniqueEmail(), DEFAULT_PASSWORD, false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]", startsWith("acceptedTerms")));
    }

    @Test
    void shouldRejectDuplicatedEmail() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Outra", email.toUpperCase(), DEFAULT_PASSWORD, true)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRejectWeakPassword() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Senha Fraca", uniqueEmail(), "abc", true)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldLoginAndAccessOwnData() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);

        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();
        String token = com.jayway.jsonpath.JsonPath.read(body, "$.token");

        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void shouldRejectWrongPasswordAndAuditTheAttempt() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "SenhaErrada1")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha inválidos."));

        assertThat(auditLogRepository.findAll())
                .anyMatch(log -> log.getAction() == AuditAction.LOGIN_FAILURE && email.equals(log.getUserEmail()));
    }

    @Test
    void shouldReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn401WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer token-falso"))
                .andExpect(status().isUnauthorized());
    }
}
