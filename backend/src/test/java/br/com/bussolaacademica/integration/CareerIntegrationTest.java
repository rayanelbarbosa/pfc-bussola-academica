package br.com.bussolaacademica.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_EMAIL;
import static br.com.bussolaacademica.support.AuthTestHelper.ADMIN_PASSWORD;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.login;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Cadastro de carreiras pelo administrador e o efeito na recomendação do teste. */
@SpringBootTest
@AutoConfigureMockMvc
class CareerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String json(String name, String area, String primary, String secondary) {
        return """
                {"name":"%s","area":"%s","primaryCategory":"%s","secondaryCategory":"%s",
                 "description":"Descrição de teste da carreira."}""".formatted(name, area, primary, secondary);
    }

    /** Nota 5 em Realista (perguntas 1 e 2) e Investigativo (3 e 4), 2 nas demais: R = I = 10, o resto 4. */
    private static String answersForEducation() {
        String answers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + (id <= 4 ? 5 : 2) + "}")
                .collect(Collectors.joining(","));
        return "{\"answers\":[" + answers + "],\"interestAreas\":[\"EDUCATION\"]}";
    }

    private String createCareer(String admin, String name) throws Exception {
        String body = mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(name, "EDUCATION", "REALISTIC", "INVESTIGATIVE")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("RI"))
                .andExpect(jsonPath("$.areaLabel").value("Educação"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        return String.valueOf((Integer) JsonPath.read(body, "$.id"));
    }

    @Test
    void studentCannotManageCareers() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());
        mockMvc.perform(get("/api/admin/careers").header("Authorization", bearer(student)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Teste", "HEALTH", "SOCIAL", "INVESTIGATIVE")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListsSeededCareersAndOptions() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(get("/api/admin/careers").param("area", "HEALTH").header("Authorization", bearer(admin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(7)))
                .andExpect(jsonPath("$.content[*].name", hasItem("Medicina")));
        mockMvc.perform(get("/api/admin/careers/areas").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$", hasSize(7)))
                .andExpect(jsonPath("$[*].value", not(hasItem("UNDECIDED"))));
        mockMvc.perform(get("/api/admin/careers/categories").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$", hasSize(6)));
    }

    @Test
    void rejectsInvalidCareers() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Repetida", "HEALTH", "SOCIAL", "SOCIAL")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("Sem área", "UNDECIDED", "SOCIAL", "ARTISTIC")))
                .andExpect(status().isUnprocessableEntity());
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("medicina", "HEALTH", "INVESTIGATIVE", "SOCIAL")))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"area\":\"HEALTH\"}"))
                .andExpect(status().isBadRequest());
        // 3º tipo repetindo o principal ou o secundário
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Terceiro repetido","area":"HEALTH","primaryCategory":"SOCIAL",
                                 "secondaryCategory":"INVESTIGATIVE","tertiaryCategory":"SOCIAL","description":"d"}"""))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void careerWithThirdTypeHasThreeLetterCode() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String name = "Bioinformática " + UUID.randomUUID().toString().substring(0, 6);
        mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","area":"HEALTH","primaryCategory":"INVESTIGATIVE",
                                 "secondaryCategory":"CONVENTIONAL","tertiaryCategory":"REALISTIC","description":"d"}"""
                                .formatted(name)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ICR"))
                .andExpect(jsonPath("$.tertiaryCategoryLabel").value("Realista"));

        // as carreiras iniciais também ganharam o 3º tipo (V17)
        mockMvc.perform(get("/api/admin/careers").param("name", "Medicina Veterinária").header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.content[0].code").value("RIC"))
                .andExpect(jsonPath("$.content[0].onetCode").value("29-1131.00"));
    }

    @Test
    void newCareerEntersTheRecommendationAndLeavesWhenDeactivated() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String name = "Engenheiro de Prompt " + UUID.randomUUID().toString().substring(0, 6);
        String id = createCareer(admin, name);

        // R = I = 10: compatibilidade 2 x 10 + 10 = 30, a maior possível; entra no topo da área Educação
        String student = registerStudent(mockMvc, uniqueEmail());
        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersForEducation()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.careerRecommendations[*].name", hasItem(name)));

        mockMvc.perform(put("/api/admin/careers/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(name, "EDUCATION", "INVESTIGATIVE", "REALISTIC")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("IR"));

        mockMvc.perform(patch("/api/admin/careers/" + id + "/status")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersForEducation()))
                .andExpect(jsonPath("$.careerRecommendations[*].name", not(hasItem(name))));

        // carreira desativada também não aceita busca de vídeos
        mockMvc.perform(get("/api/videos").param("career", name).header("Authorization", bearer(student)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/admin/careers").param("name", name).param("active", "false")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.totalElements").value(1));
        mockMvc.perform(get("/api/admin/audit-logs").param("action", "CAREER_STATUS_CHANGED")
                        .header("Authorization", bearer(admin)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    /**
     * Com os dados reais (V16 a V18): perfil R = 10, I = 8, C = 8 e o resto 2. As 5 engenharias RIC empatam em 28;
     * antes o desempate era pela ordem alfabética e Mecânica, de Computação e Química nunca apareciam.
     * Com o perfil completo do O*NET, são justamente as 3 primeiras do ranking desse aluno.
     */
    @Test
    void sameCodeCareersAreRankedByTheFullOnetProfile() throws Exception {
        String student = registerStudent(mockMvc, uniqueEmail());
        int[] answers = {5, 5, 4, 4, 1, 1, 1, 1, 1, 1, 4, 4};
        String body = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + answers[id - 1] + "}")
                .collect(Collectors.joining(",", "{\"answers\":[", "],\"interestAreas\":[\"ENGINEERING\"]}"));

        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(student))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.careerRecommendations", hasSize(4)))
                .andExpect(jsonPath("$.careerRecommendations[0:3].name",
                        org.hamcrest.Matchers.contains("Engenharia Mecânica", "Engenharia de Computação", "Engenharia Química")))
                // dia a dia vindo do O*NET (V19)
                .andExpect(jsonPath("$.careerRecommendations[0].dailyTasks", hasSize(3)))
                .andExpect(jsonPath("$.careerRecommendations[0].dailyTasks[0]").value("Lê e interpreta desenhos técnicos e projetos"));
    }

    @Test
    void adminManagesDailyTasks() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        String name = "Dia a dia " + UUID.randomUUID().toString().substring(0, 6);
        String base = "{\"name\":\"" + name + "\",\"area\":\"HEALTH\",\"primaryCategory\":\"SOCIAL\","
                + "\"secondaryCategory\":\"INVESTIGATIVE\",\"description\":\"d\",\"dailyTasks\":";
        String body = mockMvc.perform(post("/api/admin/careers")
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base + "[\" Atende pacientes \",\"\",\"Faz relatórios\"]}"))
                .andExpect(status().isCreated())
                // tarefas vazias são descartadas e os espaços, removidos
                .andExpect(jsonPath("$.dailyTasks", org.hamcrest.Matchers.contains("Atende pacientes", "Faz relatórios")))
                .andReturn().getResponse().getContentAsString();
        String id = String.valueOf((Integer) JsonPath.read(body, "$.id"));

        // mais de 3 tarefas ou o separador "|" são recusados
        mockMvc.perform(put("/api/admin/careers/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base + "[\"a\",\"b\",\"c\",\"d\"]}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/admin/careers/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base + "[\"a|b\"]}"))
                .andExpect(status().isBadRequest());

        // lista vazia limpa as tarefas
        mockMvc.perform(put("/api/admin/careers/" + id)
                        .header("Authorization", bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base + "[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyTasks", hasSize(0)));
    }

    @Test
    void unknownCareerReturnsNotFound() throws Exception {
        String admin = login(mockMvc, ADMIN_EMAIL, ADMIN_PASSWORD);
        mockMvc.perform(get("/api/admin/careers/999999").header("Authorization", bearer(admin)))
                .andExpect(status().isNotFound());
    }
}
