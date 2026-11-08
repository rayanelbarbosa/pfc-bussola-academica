package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.Role;
import jakarta.validation.constraints.NotNull;

/** Novo perfil de acesso de um usuário (STUDENT ou ADMIN). */
public record RoleChangeRequest(@NotNull(message = "informe o perfil") Role role) {
}
