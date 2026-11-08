package br.com.bussolaacademica.integration;

import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.AuditLog;
import br.com.bussolaacademica.repository.AuditLogRepository;
import br.com.bussolaacademica.repository.TermsVersionRepository;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.service.AuditService;
import br.com.bussolaacademica.service.TermsService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Period;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.DEFAULT_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.loginJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Bloco de segurança e LGPD: idade mínima, logout real, força bruta, exportação, novo aceite e expurgo. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityLgpdIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TermsVersionRepository termsVersionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private AuditService auditService;
    @Autowired
    private TermsService termsService;

    /** Os testes compartilham o banco H2: desfaz a versão nova dos termos para não afetar as outras classes. */
    @AfterEach
    void restoreTermsVersion() {
        termsVersionRepository.findAll().stream()
                .filter(terms -> !"1.0".equals(terms.getVersion()))
                .forEach(termsVersionRepository::delete);
        userRepository.findAll().forEach(user -> {
            if (!"1.0".equals(user.getTermsVersion())) {
                user.acceptTerms("1.0", user.getTermsAcceptedAt());
                userRepository.save(user);
            }
        });
        termsService.reload();
    }

    @Test
    void shouldRejectRegistrationWithoutDeclaringMinimumAge() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Menor", uniqueEmail(), DEFAULT_PASSWORD, false, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0]", startsWith("ageConfirmed")));
    }

    @Test
    void shouldRevokeTokenOnLogout() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/logout").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRequireTokenToLogout() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldBlockLoginAfterFiveFailuresEvenWithRightPassword() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(email, "SenhaErrada9")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, DEFAULT_PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message", containsString("15 minutos")));
        assertThat(auditLogRepository.findAll())
                .anyMatch(log -> log.getAction() == AuditAction.LOGIN_BLOCKED && email.equals(log.getUserEmail()));
    }

    @Test
    void successfulLoginResetsFailureCount() throws Exception {
        String email = uniqueEmail();
        registerStudent(mockMvc, email);
        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson(email, "SenhaErrada9")));
        }
        login(mockMvc, email, DEFAULT_PASSWORD);
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(loginJson(email, "SenhaErrada9")));

        login(mockMvc, email, DEFAULT_PASSWORD);
    }

    @Test
    void shouldExportPersonalDataAsJsonAttachment() throws Exception {
        String email = uniqueEmail();
        String token = registerStudent(mockMvc, email);

        mockMvc.perform(get("/api/users/me/export").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("attachment")))
                .andExpect(jsonPath("$.account.email").value(email))
                .andExpect(jsonPath("$.account.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.vocationalTestResults").isArray())
                .andExpect(jsonPath("$.activityLog[0].action").value("Exportação dos dados pessoais"))
                .andExpect(jsonPath("$.activityLog[0].userEmail").doesNotExist())
                .andExpect(jsonPath("$.activityLog[0].resource").doesNotExist());
    }

    @Test
    void shouldRequireNewAcceptanceAfterAdminPublishesNewTerms() throws Exception {
        String studentEmail = uniqueEmail();
        String oldStudentToken = registerStudent(mockMvc, studentEmail);
        String adminToken = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(post("/api/admin/terms").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishJson("9.9", "Novo prazo de guarda dos registros")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.version").value("9.9"));

        // Token antigo: só libera os próprios dados e o novo aceite
        mockMvc.perform(get("/api/users/me/results").header(HttpHeaders.AUTHORIZATION, bearer(oldStudentToken)))
                .andExpect(status().is(428));
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(oldStudentToken)))
                .andExpect(status().isOk());

        // Login avisa que há termos pendentes
        String loginBody = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(studentEmail, DEFAULT_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingTerms.version").value("9.9"))
                .andReturn().getResponse().getContentAsString();
        String pendingToken = JsonPath.read(loginBody, "$.token");

        // Aceite de versão errada é recusado; o da vigente devolve um token liberado
        mockMvc.perform(post("/api/terms/accept").header(HttpHeaders.AUTHORIZATION, bearer(pendingToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"1.0\"}"))
                .andExpect(status().isUnprocessableEntity());
        String acceptBody = mockMvc.perform(post("/api/terms/accept")
                        .header(HttpHeaders.AUTHORIZATION, bearer(pendingToken))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"version\":\"9.9\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.termsVersion").value("9.9"))
                .andExpect(jsonPath("$.pendingTerms").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        mockMvc.perform(get("/api/users/me/results")
                        .header(HttpHeaders.AUTHORIZATION, bearer(JsonPath.read(acceptBody, "$.token"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotPublishDuplicatedTermsVersion() throws Exception {
        String adminToken = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(post("/api/admin/terms").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishJson("1.0", "Repetida")))
                .andExpect(status().isConflict());
    }

    @Test
    void studentCannotPublishTerms() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());
        mockMvc.perform(post("/api/admin/terms").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishJson("5.0", "Tentativa")))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldExposeCurrentTermsPublicly() throws Exception {
        mockMvc.perform(get("/api/terms/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"))
                .andExpect(jsonPath("$.termsOfUse", containsString("## 1.")))
                .andExpect(jsonPath("$.privacyPolicy", containsString("LGPD")));
    }

    @Test
    void shouldServeTheTextsPublishedByTheAdmin() throws Exception {
        String adminToken = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(post("/api/admin/terms").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(publishJson("8.0", "Texto revisado")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/terms/current"))
                .andExpect(jsonPath("$.version").value("8.0"))
                .andExpect(jsonPath("$.termsOfUse").value("Novo termo 8.0"))
                .andExpect(jsonPath("$.privacyPolicy").value("Nova política 8.0"));
    }

    @Test
    void shouldRejectPublicationWithoutTexts() throws Exception {
        String adminToken = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(post("/api/admin/terms").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"version\":\"7.0\",\"summary\":\"Sem texto\",\"termsOfUse\":\"\",\"privacyPolicy\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    private static String publishJson(String version, String summary) {
        return "{\"version\":\"" + version + "\",\"summary\":\"" + summary + "\",\"termsOfUse\":\"Novo termo "
                + version + "\",\"privacyPolicy\":\"Nova política " + version + "\"}";
    }

    @Test
    void shouldPurgeAuditLogsOlderThanSixMonths() {
        AuditLog old = auditLogRepository.save(new AuditLog(LocalDateTime.now().minusMonths(7), null,
                "antigo@teste.com", AuditAction.LOGIN_SUCCESS, null, null, null, true));
        AuditLog recent = auditLogRepository.save(new AuditLog(LocalDateTime.now().minusMonths(5), null,
                "recente@teste.com", AuditAction.LOGIN_SUCCESS, null, null, null, true));

        int deleted = auditService.purgeOlderThan(Period.ofMonths(6));

        assertThat(deleted).isGreaterThanOrEqualTo(1);
        assertThat(auditLogRepository.findById(old.getId())).isEmpty();
        assertThat(auditLogRepository.findById(recent.getId())).isPresent();
        assertThat(auditLogRepository.findAll()).anyMatch(log -> log.getAction() == AuditAction.AUDIT_LOGS_PURGED);
    }

    @Test
    void shouldAuditAccessDeniedRaisedInsideServices() throws Exception {
        String ownerToken = registerStudent(mockMvc, uniqueEmail());
        String body = mockMvc.perform(post("/api/vocational-test/results")
                        .header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(fullAnswersJson()))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer resultId = JsonPath.read(body, "$.id");
        String otherEmail = uniqueEmail();
        String otherToken = registerStudent(mockMvc, otherEmail);

        mockMvc.perform(get("/api/vocational-test/results/" + resultId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isForbidden());

        assertThat(auditLogRepository.findAll())
                .anyMatch(log -> log.getAction() == AuditAction.ACCESS_DENIED && otherEmail.equals(log.getUserEmail()));
    }

    private static String fullAnswersJson() {
        return java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":3}")
                .collect(java.util.stream.Collectors.joining(",", "{\"answers\":[", "]}"));
    }
}
