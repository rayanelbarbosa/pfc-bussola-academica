package br.com.bussolaacademica.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


import static br.com.bussolaacademica.support.AuthTestHelper.answers;
import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste de ponta a ponta no back-end: sobe a aplicação inteira com banco H2,
 * aplica as migrations do Flyway e percorre o fluxo real do teste vocacional.
 */
@SpringBootTest
@AutoConfigureMockMvc
class VocationalTestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAnswerQuestionnaireSaveAndFetchResult() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(get("/api/vocational-test/questions").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(30)));

        // Nota 4 nas 5 atividades de Investigativo e 2 nas demais
        String answers = "{\"answers\":[" + answers(2, 4, 2, 2, 2, 2) + "]}";

        String body = mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answers))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scores[1].score").value(20))
                .andExpect(jsonPath("$.maxScore").value(20))
                .andExpect(jsonPath("$.topCategories[0].category").value("INVESTIGATIVE"))
                .andReturn().getResponse().getContentAsString();

        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(get("/api/vocational-test/results/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.careerRecommendations").isArray())
                .andExpect(jsonPath("$.recommendedAreas").doesNotExist());
    }

    /** Nota 4 em Realista e Investigativo, 1 nas demais: R = I = 20, o resto 5. */
    private static String answersWithAreas(String areasJson) {
        return "{\"answers\":[" + answers(4, 4, 1, 1, 1, 1) + "],\"interestAreas\":" + areasJson + "}";
    }

    @Test
    void shouldRecommendCareersForEachChosenArea() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(get("/api/vocational-test/interest-areas").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(8)));

        String body = mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersWithAreas("[\"ENGINEERING\",\"HEALTH\"]")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.interestAreas[0].value").value("ENGINEERING"))
                // ranking único das 2 áreas: as 4 melhores, cada uma com a sua área
                .andExpect(jsonPath("$.careerRecommendations", hasSize(4)))
                // R=20, I=20: carreiras RI e IR empatam com 2 × 20 + 20 = 60 pontos
                .andExpect(jsonPath("$.careerRecommendations[0].compatibility").value(60))
                .andExpect(jsonPath("$.careerRecommendations[0].areaLabel").isNotEmpty())
                // comparação entre as áreas escolhidas
                .andExpect(jsonPath("$.areaMatches", hasSize(2)))
                .andExpect(jsonPath("$.areaMatches[*].area", org.hamcrest.Matchers.containsInAnyOrder("ENGINEERING", "HEALTH")))
                .andReturn().getResponse().getContentAsString();

        Integer id = JsonPath.read(body, "$.id");
        mockMvc.perform(get("/api/vocational-test/results/" + id).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.careerRecommendations", hasSize(4)))
                .andExpect(jsonPath("$.areaMatches", hasSize(2)));
        mockMvc.perform(get("/api/users/me/results").header("Authorization", bearer(token)))
                .andExpect(jsonPath("$[0].interestAreas[0]").value("Engenharias"));
    }

    @Test
    void shouldRejectInvalidInterestAreas() throws Exception {
        String token = registerStudent(mockMvc, uniqueEmail());

        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersWithAreas("[\"HEALTH\",\"ENGINEERING\",\"BUSINESS\",\"EDUCATION\"]")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersWithAreas("[\"UNDECIDED\",\"HEALTH\"]")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answersWithAreas("[\"UNDECIDED\"]")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.careerRecommendations", hasSize(4)))
                .andExpect(jsonPath("$.areaMatches", hasSize(7)));
    }

    @Test
    void shouldExposeHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
