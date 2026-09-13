package br.com.bussolaacademica.testevocacional;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TesteVocacionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveListarPerguntasViaApi() throws Exception {
        mockMvc.perform(get("/api/teste-vocacional/perguntas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    @Test
    void deveSubmeterRespostasERetornarResultadoCalculado() throws Exception {
        SubmeterTesteRequest request = new SubmeterTesteRequest(java.util.List.of(
                new RespostaDTO(5, 5),
                new RespostaDTO(6, 5)
        ));

        mockMvc.perform(post("/api/teste-vocacional")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoriasPrincipais[0]").value("Artístico"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void deveRetornar400QuandoPerguntaNaoExiste() throws Exception {
        SubmeterTesteRequest request = new SubmeterTesteRequest(java.util.List.of(
                new RespostaDTO(9999, 3)
        ));

        mockMvc.perform(post("/api/teste-vocacional")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar400QuandoNotaForaDaFaixaPermitida() throws Exception {
        SubmeterTesteRequest request = new SubmeterTesteRequest(java.util.List.of(
                new RespostaDTO(1, 9)
        ));

        mockMvc.perform(post("/api/teste-vocacional")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
