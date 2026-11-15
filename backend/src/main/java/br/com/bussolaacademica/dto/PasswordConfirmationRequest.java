package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotBlank;

/** Senha de quem está logado, pedida de novo antes de uma ação que não pode ser desfeita. */
public record PasswordConfirmationRequest(@NotBlank(message = "informe a sua senha") String password) {
}
