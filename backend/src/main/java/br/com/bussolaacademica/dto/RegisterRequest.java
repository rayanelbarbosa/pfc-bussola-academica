package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados do cadastro. Só o mínimo necessário (LGPD: princípio da necessidade). */
public record RegisterRequest(
        @NotBlank(message = "o nome é obrigatório")
        @Size(max = 100, message = "o nome deve ter no máximo 100 caracteres") String name,
        @NotBlank(message = "o e-mail é obrigatório")
        @Email(message = "e-mail inválido")
        @Size(max = 150, message = "o e-mail deve ter no máximo 150 caracteres") String email,
        @NotBlank(message = "a senha é obrigatória")
        @Size(min = 8, max = 72, message = "a senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "a senha deve ter letras e números") String password,
        @NotNull(message = "é necessário aceitar os Termos de Uso e a Política de Privacidade")
        @AssertTrue(message = "é necessário aceitar os Termos de Uso e a Política de Privacidade") Boolean acceptedTerms
) {
}
