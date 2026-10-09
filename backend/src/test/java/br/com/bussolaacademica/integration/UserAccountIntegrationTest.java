package br.com.bussolaacademica.integration;

import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static br.com.bussolaacademica.support.AuthTestHelper.bearer;
import static br.com.bussolaacademica.support.AuthTestHelper.registerStudent;
import static br.com.bussolaacademica.support.AuthTestHelper.uniqueEmail;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integração da conta do usuário: sobem a aplicação inteira (controller, service e
 * repository) com banco H2 em memória e migrations do Flyway. Cada teste cria o próprio usuário.
 */
@SpringBootTest
@AutoConfigureMockMvc
class UserAccountIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VocationalTestResultRepository resultRepository;

    @Test
    void deveRetornar200ComOsDadosDoUsuarioLogadoSemExporASenha() throws Exception {
        // Arrange
        String email = uniqueEmail();
        String token = registerStudent(mockMvc, email);

        // Act + Assert
        mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.termsVersion").value("1.0"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void deveRetornar404ComCorpoDeErroQuandoResultadoNaoExiste() throws Exception {
        // Arrange
        String token = registerStudent(mockMvc, uniqueEmail());

        // Act + Assert
        mockMvc.perform(get("/api/vocational-test/results/999999").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Resultado não encontrado: id=999999"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deveEnviarTesteEExibirNoHistoricoComOResultadoGravadoNoBanco() throws Exception {
        // Arrange: nota 5 nas perguntas de Investigativo (3 e 4) e 3 nas demais
        String token = registerStudent(mockMvc, uniqueEmail());
        String answers = IntStream.rangeClosed(1, 12)
                .mapToObj(id -> "{\"questionId\":" + id + ",\"score\":" + (id == 3 || id == 4 ? 5 : 3) + "}")
                .collect(Collectors.joining(",", "{\"answers\":[", "]}"));

        // Act: API -> service -> banco
        mockMvc.perform(post("/api/vocational-test/results")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(answers))
                .andExpect(status().isCreated());

        // Assert: histórico pela API
        mockMvc.perform(get("/api/users/me/results").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].topCategories[0]").value("Investigativo"));

        // Assert: registro gravado no banco para o usuário certo
        String me = mockMvc.perform(get("/api/users/me").header("Authorization", bearer(token)))
                .andReturn().getResponse().getContentAsString();
        Long userId = ((Number) JsonPath.read(me, "$.id")).longValue();
        List<VocationalTestResult> salvos = resultRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        assertEquals(1, salvos.size());
        assertTrue(salvos.get(0).getRecommendedAreas().contains("Medicina"));
    }
}
