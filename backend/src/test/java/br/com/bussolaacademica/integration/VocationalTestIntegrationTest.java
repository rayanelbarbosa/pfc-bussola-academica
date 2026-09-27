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
                .andExpect(jsonPath("$.recommendedAreas", hasSize(3)));
    }

    @Test
    void shouldExposeHealthCheck() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
