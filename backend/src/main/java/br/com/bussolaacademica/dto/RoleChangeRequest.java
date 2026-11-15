package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Novo perfil de acesso de um usuário (STUDENT ou ADMIN) e a senha do administrador, pedida de novo. */
public record RoleChangeRequest(@NotNull(message = "informe o perfil") Role role,
                                @NotBlank(message = "informe a sua senha") String password) {
}
