package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Resposta do usuário a uma pergunta, em escala Likert de 1 a 5
 * (1 = discordo totalmente, 5 = concordo totalmente).
 */
public record AnswerRequest(
        @NotNull(message = "questionId é obrigatório") Integer questionId,
        @NotNull(message = "a nota é obrigatória")
        @Min(value = 1, message = "a nota mínima é 1")
        @Max(value = 5, message = "a nota máxima é 5") Integer score
) {
}
