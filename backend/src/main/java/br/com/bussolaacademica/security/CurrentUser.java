package br.com.bussolaacademica.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Acesso ao usuário autenticado da requisição atual (lido do JWT). */
@Component
public class CurrentUser {

    public Long id() {
        return jwt().map(jwt -> Long.valueOf(jwt.getSubject()))
                .orElseThrow(() -> new IllegalStateException("Nenhum usuário autenticado"));
    }

    public Optional<Long> idIfAuthenticated() {
        return jwt().map(jwt -> Long.valueOf(jwt.getSubject()));
    }

    public Optional<String> emailIfAuthenticated() {
        return jwt().map(jwt -> jwt.getClaimAsString(TokenService.EMAIL_CLAIM));
    }

    public boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private Optional<Jwt> jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }
}
