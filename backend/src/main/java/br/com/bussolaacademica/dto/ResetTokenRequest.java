package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Token do link de redefinição, enviado no corpo (e não na URL) para não ficar em logs de acesso. */
public record ResetTokenRequest(
        @NotBlank(message = "o link de redefinição é obrigatório")
        @Size(max = 100, message = "link de redefinição inválido") String token
) {
}
