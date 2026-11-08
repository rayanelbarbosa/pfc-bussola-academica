package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Nova versão do Termo/Política publicada pelo administrador: número, resumo da mudança e os dois textos. */
public record PublishTermsRequest(
        @NotBlank(message = "a versão é obrigatória")
        @Size(max = 20, message = "a versão deve ter no máximo 20 caracteres")
        @Pattern(regexp = "^\\d+(\\.\\d+){0,2}$", message = "use números separados por ponto (ex.: 1.1)") String version,
        @NotBlank(message = "o resumo da mudança é obrigatório")
        @Size(max = 500, message = "o resumo deve ter no máximo 500 caracteres") String summary,
        @NotBlank(message = "o texto do Termo de Uso é obrigatório")
        @Size(max = 20000, message = "o Termo de Uso deve ter no máximo 20.000 caracteres") String termsOfUse,
        @NotBlank(message = "o texto da Política de Privacidade é obrigatório")
        @Size(max = 20000, message = "a Política de Privacidade deve ter no máximo 20.000 caracteres") String privacyPolicy
) {
}
