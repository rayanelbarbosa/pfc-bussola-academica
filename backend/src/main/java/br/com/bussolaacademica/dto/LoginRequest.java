package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "o e-mail é obrigatório") String email,
        @NotBlank(message = "a senha é obrigatória") String password
) {
}
