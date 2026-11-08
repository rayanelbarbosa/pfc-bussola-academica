package br.com.bussolaacademica.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Formas de ingresso: consulta pública (RF01) e gestão pelo administrador (RF11). */
@SpringBootTest
@AutoConfigureMockMvc
class AdmissionMethodIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String json(String name, String type, String url) {
        return """
                {"name":"%s","type":"%s","summary":"Resumo de teste","description":"O que é",
                 "howItWorks":"Passo 1\\nPasso 2","eligibility":"Quem concluiu o ensino médio",
                 "schedule":"Todo semestre","cost":"Gratuito","officialUrl":"%s"}""".formatted(name, type, url);
    }

    @Test
    void visitorListsActiveMethodsWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/admission-methods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(8)))
                .andExpect(jsonPath("$[0].name").value("Enem"))
                .andExpect(jsonPath("$[0].howItWorks.length()", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$[*].active", everyItem(is(true))));

        mockMvc.perform(get("/api/admission-methods").param("type", "SCHOLARSHIP_PORTAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", everyItem(is("SCHOLARSHIP_PORTAL"))));

        mockMvc.perform(get("/api/admission-methods/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test
    void studentCannotManageMethods() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());
        mockMvc.perform(post("/api/admin/admission-methods")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teste", "ENTRANCE_EXAM", "https://exemplo.com")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesUpdatesAndDeactivatesMethod() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String name = "Vestibular " + UUID.randomUUID().toString().substring(0, 8);

        String body = mockMvc.perform(post("/api/admin/admission-methods")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(name, "ENTRANCE_EXAM", "https://exemplo.com")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.typeLabel").value("Vestibular"))
                .andExpect(jsonPath("$.howItWorks", hasSize(2)))
                .andReturn().getResponse().getContentAsString();
        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(post("/api/admin/admission-methods")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(name.toUpperCase(), "ENTRANCE_EXAM", "https://exemplo.com")))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/admin/admission-methods/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(name, "NATIONAL_EXAM", "https://exemplo.com/novo")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.officialUrl").value("https://exemplo.com/novo"));

        mockMvc.perform(get("/api/admission-methods"))
                .andExpect(jsonPath("$[*].name", hasItem(name)));

        mockMvc.perform(patch("/api/admin/admission-methods/" + id + "/status")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/admission-methods"))
                .andExpect(jsonPath("$[*].name", not(hasItem(name))));

        mockMvc.perform(get("/api/admin/admission-methods")
                        .param("name", name).param("active", "false")
                        .header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/admin/audit-logs")
                        .param("action", "ADMISSION_METHOD_STATUS_CHANGED")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    void methodWithoutOfficialLinkIsAccepted() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String name = "Bolsa " + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/admin/admission-methods")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(name, "SCHOLARSHIP_PORTAL", "")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.officialUrl").doesNotExist());
    }

    @Test
    void invalidDataIsRejected() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(post("/api/admin/admission-methods")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "ENTRANCE_EXAM", "ftp://nao-vale")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasSize(greaterThanOrEqualTo(2))));

        mockMvc.perform(get("/api/admin/admission-methods/999999").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }
}
