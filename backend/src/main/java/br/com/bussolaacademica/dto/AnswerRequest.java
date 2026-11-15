package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Resposta do usuário a uma atividade, na escala do O*NET Interest Profiler, de 0 a 4
 * (0 = detesto, 1 = não gosto, 2 = não sei, 3 = gosto, 4 = adoro).
 */
public record AnswerRequest(
        @NotNull(message = "questionId é obrigatório") Integer questionId,
        @NotNull(message = "a nota é obrigatória")
        @Min(value = 0, message = "a nota mínima é 0")
        @Max(value = 4, message = "a nota máxima é 4") Integer score
) {
}
