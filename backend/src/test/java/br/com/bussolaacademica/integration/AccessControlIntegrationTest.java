package br.com.bussolaacademica.integration;

import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.AuditLogRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.DEFAULT_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.answers;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.loginJson;
import static br.com.bussolaacademica.support.AuthTestHelper.passwordJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Controle de acesso por perfil, posse dos dados e auditoria. */
@SpringBootTest
@AutoConfigureMockMvc
class AccessControlIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VocationalTestResultRepository resultRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void studentCannotReadAuditLogsButAdminCan() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(get("/api/admin/audit-logs").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());

        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(get("/api/admin/audit-logs").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThan(0)));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("action", "ACCESS_DENIED")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].action").value("ACCESS_DENIED"));
    }

    @Test
    void studentCanOnlySeeOwnResults() throws Exception {
        String owner = registerStudent(mockMvc, uniqueEmail());
        String other = registerStudent(mockMvc, uniqueEmail());
        Integer resultId = submitTest(owner);

        mockMvc.perform(get("/api/vocational-test/results/" + resultId).header("Authorization", bearer(owner)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/vocational-test/results/" + resultId).header("Authorization", bearer(other)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/users/me/results").header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void vocationalTestRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/vocational-test/questions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void studentCanDeleteOwnAccountAndTheResultsAreAnonymized() throws Exception {
        String email = uniqueEmail();
        String token = registerStudent(mockMvc, email);
        Integer resultId = submitTest(token);

        // sem a senha certa a conta não é excluída
        mockMvc.perform(delete("/api/users/me").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordJson("SenhaErrada1")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(delete("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/me").header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(passwordJson(DEFAULT_PASSWORD)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, DEFAULT_PASSWORD)))
                .andExpect(status().isUnauthorized());

        // LGPD: o resultado continua nas estatísticas, mas não é mais de ninguém e guarda só a data
        VocationalTestResult anonymous = resultRepository.findById(resultId.longValue()).orElseThrow();
        assertThat(anonymous.getUser()).isNull();
        assertThat(anonymous.getCreatedAt().toLocalTime()).isEqualTo(LocalTime.MIDNIGHT);
        // e a auditoria do envio do teste não guarda nada que religue o resultado ao e-mail
        assertThat(auditLogRepository.findAll())
                .filteredOn(log -> log.getAction() == AuditAction.TEST_SUBMITTED && email.equals(log.getUserEmail()))
                .isNotEmpty()
                .allMatch(log -> "Teste vocacional enviado".equals(log.getDetails()));
    }

    @Test
    void videosEndpointReportsWhenYouTubeIsNotConfigured() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(get("/api/videos").param("career", "Medicina").header("Authorization", bearer(token)))
                .andExpect(status().isBadGateway());

        mockMvc.perform(get("/api/videos").param("career", "qualquer coisa").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/videos").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());

        // perfil RIASEC é opcional; quando vem, cada valor precisa ser um tipo válido
        mockMvc.perform(get("/api/videos").param("career", "Medicina").param("profile", "SOCIAL,INVESTIGATIVE")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadGateway());
        mockMvc.perform(get("/api/videos").param("career", "Medicina").param("profile", "XYZ")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private Integer submitTest(String token) throws Exception {
        String answers = "{\"answers\":[" + answers(4, 4, 4, 4, 4, 4) + "]}";
        String body = mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answers))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}
