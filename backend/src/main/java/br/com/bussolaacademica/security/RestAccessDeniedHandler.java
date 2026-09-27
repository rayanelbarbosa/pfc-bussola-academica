package br.com.bussolaacademica.security;

import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 403: usuário autenticado tentando acessar algo que o perfil dele não permite. A tentativa é auditada. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonErrorWriter errorWriter;
    private final AuditService auditService;

    public RestAccessDeniedHandler(JsonErrorWriter errorWriter, AuditService auditService) {
        this.errorWriter = errorWriter;
        this.auditService = auditService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        auditService.recordForCurrentUser(AuditAction.ACCESS_DENIED,
                request.getMethod() + " " + request.getRequestURI(), false);
        errorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                "Você não tem permissão para acessar este recurso.");
    }
}
