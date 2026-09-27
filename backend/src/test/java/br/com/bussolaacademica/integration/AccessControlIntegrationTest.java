package br.com.bussolaacademica.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.DEFAULT_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.loginJson;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
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
    void studentCanDeleteOwnAccount() throws Exception {
        String email = uniqueEmail();
        String token = registerStudent(mockMvc, email);
        submitTest(token);

        mockMvc.perform(delete("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, DEFAULT_PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void videosEndpointReportsWhenYouTubeIsNotConfigured() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(get("/api/videos").param("area", "Medicina").header("Authorization", bearer(token)))
                .andExpect(status().isBadGateway());

        mockMvc.perform(get("/api/videos").header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest());
    }

    private Integer submitTest(String token) throws Exception {
        String answers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":4}")
                .collect(Collectors.joining(",", "{\"answers\":[", "]}"));
        String body = mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answers))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }
}
