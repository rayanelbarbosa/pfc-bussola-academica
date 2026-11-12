package br.com.bussolaacademica.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Gestão de usuários e administradores (RF12). */
@SpringBootTest
@AutoConfigureMockMvc
class AdminUserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String roleJson(String role) {
        return "{\"role\":\"" + role + "\"}";
    }

    @Test
    void adminPromotesAndDemotesAndOldTokenStopsWorking() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = uniqueEmail();
        String studentToken = registerStudent(mockMvc, email);

        String page = mockMvc.perform(get("/api/admin/users").param("search", email).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].role").value("STUDENT"))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(page, "$.content[0].id");

        mockMvc.perform(patch("/api/admin/users/" + id + "/role").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(roleJson("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        // O token antigo (perfil STUDENT) deixa de valer assim que o perfil muda
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(studentToken)))
                .andExpect(status().isUnauthorized());

        String newAdmin = login(mockMvc, email, "Senha1234");
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(newAdmin)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/users/" + id + "/role").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(roleJson("STUDENT")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(newAdmin)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/admin/audit-logs").param("action", "USER_ROLE_CHANGED").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(2)));
    }

    @Test
    void adminCannotChangeOwnRole() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String page = mockMvc.perform(get("/api/admin/users").param("search", ADMIN_EMAIL).header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString();
        Integer adminId = JsonPath.read(page, "$.content[0].id");

        mockMvc.perform(patch("/api/admin/users/" + adminId + "/role").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON).content(roleJson("STUDENT")))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void adminCreatesAnotherAdminWithValidation() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = uniqueEmail();

        mockMvc.perform(post("/api/admin/users").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nova Admin\",\"email\":\"" + email + "\",\"password\":\"fraca\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/admin/users").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Nova Admin\",\"email\":\"" + email + "\",\"password\":\"Admin2026x\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(post("/api/admin/users").header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Outra\",\"email\":\"" + email + "\",\"password\":\"Admin2026x\"}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/admin/users").param("role", "ADMIN").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.content[*].role", not(hasItem("STUDENT"))));
    }

    @Test
    void adminDeletesAnotherAccountButNotTheirOwn() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = uniqueEmail();
        String studentToken = registerStudent(mockMvc, email);
        String page = mockMvc.perform(get("/api/admin/users").param("search", email).header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(page, "$.content[0].id");

        mockMvc.perform(delete("/api/admin/users/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/admin/users").param("search", email).header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(studentToken)))
                .andExpect(status().isUnauthorized());

        String adminPage = mockMvc.perform(get("/api/admin/users").param("search", ADMIN_EMAIL).header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString();
        Integer adminId = JsonPath.read(adminPage, "$.content[0].id");
        mockMvc.perform(delete("/api/admin/users/" + adminId).header("Authorization", bearer(admin)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void adminSeesUserDetailsWithTestHistoryAndTheAccessIsAudited() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String email = uniqueEmail();
        String studentToken = registerStudent(mockMvc, email);
        String answers = java.util.stream.IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + (id == 3 || id == 4 ? 5 : 3) + "}")
                .collect(java.util.stream.Collectors.joining(",", "{\"answers\":[", "]}"));
        String result = mockMvc.perform(post("/api/vocational-test/results").header("Authorization", bearer(studentToken))
                        .contentType(MediaType.APPLICATION_JSON).content(answers))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Integer resultId = JsonPath.read(result, "$.id");
        String page = mockMvc.perform(get("/api/admin/users").param("search", email).header("Authorization", bearer(admin)))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(page, "$.content[0].id");

        mockMvc.perform(get("/api/admin/users/" + id).header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.termsVersion").isNotEmpty())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.results.length()").value(1))
                .andExpect(jsonPath("$.results[0].id").value(resultId))
                .andExpect(jsonPath("$.results[0].topCategories[0]").value("Investigativo"));

        // acesso a dado pessoal de outra pessoa: fica registrado na auditoria
        mockMvc.perform(get("/api/admin/audit-logs").param("action", "USER_DATA_VIEWED").param("email", ADMIN_EMAIL)
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.content[0].details").value(org.hamcrest.Matchers.containsString(email)));

        mockMvc.perform(get("/api/admin/users/999999").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentCannotManageUsers() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());
        mockMvc.perform(get("/api/admin/users").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/users/1").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
    }
}
