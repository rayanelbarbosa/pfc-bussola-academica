package br.com.bussolaacademica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Nova senha com o token recebido por e-mail. A senha segue a mesma regra do cadastro. */
public record ResetPasswordRequest(
        @NotBlank(message = "o link de redefinição é obrigatório")
        @Size(max = 100, message = "link de redefinição inválido") String token,
        @NotBlank(message = "a senha é obrigatória")
        @Size(min = 8, max = 72, message = "a senha deve ter entre 8 e 72 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "a senha deve ter letras e números") String newPassword
) {
}
