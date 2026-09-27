package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.CategoryScoreResponse;
import br.com.bussolaacademica.dto.QuestionResponse;
import br.com.bussolaacademica.dto.VocationalTestResultResponse;
import br.com.bussolaacademica.exception.InvalidAnswersException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.service.VocationalTestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VocationalTestController.class)
class VocationalTestControllerTest {

    private static final String BASE = "/api/vocational-test";
    private static final String ONE_ANSWER = "{\"answers\":[{\"questionId\":1,\"score\":5}]}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private VocationalTestService service;

    @Test
    void shouldListQuestions() throws Exception {
        when(service.listQuestions()).thenReturn(List.of(new QuestionResponse(1, "Enunciado", "REALISTIC")));

        mockMvc.perform(get(BASE + "/questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].category").value("REALISTIC"));
    }

    @Test
    void shouldCreateResultWithStatus201() throws Exception {
        CategoryScoreResponse investigative = new CategoryScoreResponse("INVESTIGATIVE", "Investigativo", 9);
        when(service.submit(any())).thenReturn(new VocationalTestResultResponse(
                7L, LocalDateTime.of(2026, 9, 28, 9, 0), List.of(investigative), List.of(investigative),
                List.of("Medicina")));

        mockMvc.perform(post(BASE + "/results").contentType(MediaType.APPLICATION_JSON).content(ONE_ANSWER))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.topCategories[0].label").value("Investigativo"));
    }

    @Test
    void shouldReturn400WhenScoreIsOutOfScale() throws Exception {
        mockMvc.perform(post(BASE + "/results")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":1,\"score\":6}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Dados inválidos"))
                .andExpect(jsonPath("$.details[0]").value("answers[0].score: a nota máxima é 5"));
    }

    @Test
    void shouldReturn400WhenServiceRejectsAnswers() throws Exception {
        when(service.submit(any())).thenThrow(new InvalidAnswersException("Responda todas as perguntas. Faltando: [2]"));

        mockMvc.perform(post(BASE + "/results").contentType(MediaType.APPLICATION_JSON).content(ONE_ANSWER))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Responda todas as perguntas. Faltando: [2]"));
    }

    @Test
    void shouldReturn404WhenResultDoesNotExist() throws Exception {
        when(service.findResult(99L)).thenThrow(new ResourceNotFoundException("Resultado não encontrado: id=99"));

        mockMvc.perform(get(BASE + "/results/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
