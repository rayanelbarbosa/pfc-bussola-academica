package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank(message = "o e-mail é obrigatório")
        @Email(message = "e-mail inválido")
        @Size(max = 150, message = "o e-mail deve ter no máximo 150 caracteres") String email
) {
}
