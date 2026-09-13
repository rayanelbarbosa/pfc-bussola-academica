package br.com.bussolaacademica.testevocacional;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Resposta do usuario a uma pergunta especifica, em escala Likert de 1 a 5
 * (1 = discordo totalmente, 5 = concordo totalmente).
 */
public record RespostaDTO(
        @NotNull Integer perguntaId,
        @NotNull @Min(1) @Max(5) Integer nota
) {
}
