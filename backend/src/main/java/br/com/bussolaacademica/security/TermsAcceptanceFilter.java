package br.com.bussolaacademica.security;

import br.com.bussolaacademica.service.TermsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * Novo aceite obrigatório: se o administrador publicou uma versão nova do Termo/Política e o usuário
 * ainda não aceitou, a API responde 428 (Precondition Required) e só libera o próprio aceite,
 * a consulta/exportação/exclusão dos dados pessoais e o logout.
 */
public class TermsAcceptanceFilter extends OncePerRequestFilter {

    public static final int STATUS_TERMS_PENDING = 428;

    private static final Set<String> ALLOWED = Set.of(
            "GET /api/terms/current",
            "POST /api/terms/accept",
            "POST /api/auth/logout",
            "GET /api/users/me",
            "GET /api/users/me/export",
            "DELETE /api/users/me");

    private final TermsService termsService;
    private final JsonErrorWriter errorWriter;

    public TermsAcceptanceFilter(TermsService termsService, JsonErrorWriter errorWriter) {
        this.termsService = termsService;
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt
                && !termsService.isCurrent(jwt.getClaimAsString(TokenService.TERMS_CLAIM))
                && !ALLOWED.contains(request.getMethod() + " " + request.getRequestURI())) {
            errorWriter.write(response, STATUS_TERMS_PENDING,
                    "Publicamos uma nova versão do Termo de Uso e da Política de Privacidade. Aceite para continuar.");
            return;
        }
        chain.doFilter(request, response);
    }
}
