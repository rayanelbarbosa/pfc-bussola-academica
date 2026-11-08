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
                .andExpect(jsonPath("$", hasSize(12)));

        // Nota 5 nas perguntas de Investigativo (3 e 4) e 3 nas demais
        String answers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + (id == 3 || id == 4 ? 5 : 3) + "}")
                .collect(Collectors.joining(",", "{\"answers\":[", "]}"));

        String body = mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answers))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scores[1].score").value(10))
                .andExpect(jsonPath("$.topCategories[0].category").value("INVESTIGATIVE"))
                .andReturn().getResponse().getContentAsString();

        Integer id = JsonPath.read(body, "$.id");

        mockMvc.perform(get("/api/vocational-test/results/" + id).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.careerRecommendations").isArray())
                .andExpect(jsonPath("$.recommendedAreas").doesNotExist());
    }

    /** Nota 5 nas perguntas de Realista (1 e 2) e Investigativo (3 e 4), 2 nas demais. */
    private static String answersWithAreas(String areasJson) {
        String answers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + (id <= 4 ? 5 : 2) + "}")
                .collect(Collectors.joining(","));
        return "{\"answers\":[" + answers + "],\"interestAreas\":" + areasJson + "}";
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
                .andExpect(jsonPath("$.careerRecommendations", hasSize(2)))
                .andExpect(jsonPath("$.careerRecommendations[0].label").value("Engenharias"))
                // R=10, I=10: carreiras RI e IR empatam com 30 pontos
                .andExpect(jsonPath("$.careerRecommendations[0].careers[0].compatibility").value(30))
                .andExpect(jsonPath("$.careerRecommendations[1].area").value("HEALTH"))
                .andReturn().getResponse().getContentAsString();

        Integer id = JsonPath.read(body, "$.id");
        mockMvc.perform(get("/api/vocational-test/results/" + id).header("Authorization", bearer(token)))
                .andExpect(jsonPath("$.careerRecommendations", hasSize(2)));
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
                .andExpect(jsonPath("$.careerRecommendations[0].label").value("Todas as áreas"));
    }

    @Test
    void shouldExposeHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
