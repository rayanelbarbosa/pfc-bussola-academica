package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.User;

import java.time.LocalDateTime;

/**
 * Usuário na lista do painel administrativo (sem senha, hash ou dados do teste).
 * O e-mail não vai na lista (LGPD, minimização): só aparece no detalhe, em {@link AdminUserDetailResponse},
 * acesso que fica registrado na auditoria.
 */
public record AdminUserResponse(Long id, String name, String role, LocalDateTime createdAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getRole().name(), user.getCreatedAt());
    }
}
