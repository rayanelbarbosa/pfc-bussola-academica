package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.User;

import java.time.LocalDateTime;

/** Dados do usuário expostos pela API (nunca inclui a senha nem o hash). */
public record UserResponse(Long id, String name, String email, String role,
                           LocalDateTime termsAcceptedAt, String termsVersion, LocalDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole().name(),
                user.getTermsAcceptedAt(), user.getTermsVersion(), user.getCreatedAt());
    }
}
