package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.User;

import java.time.LocalDateTime;

/** Usuário na lista do painel administrativo (sem senha, hash ou dados do teste). */
public record AdminUserResponse(Long id, String name, String email, String role, LocalDateTime createdAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
                user.getCreatedAt());
    }
}
