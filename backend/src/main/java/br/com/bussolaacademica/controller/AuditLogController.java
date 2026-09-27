package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AuditLogResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.service.AuditService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Consulta dos logs de auditoria: rota /api/admin/**, liberada só para o perfil ADMIN (SecurityConfig). */
@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> search(@RequestParam(required = false) AuditAction action,
                                                 @RequestParam(required = false) String email,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        auditService.recordForCurrentUser(AuditAction.AUDIT_LOG_VIEWED,
                "filtros: acao=" + action + ", email=" + email, true);
        return auditService.search(action, email, page, size);
    }

    @GetMapping("/actions")
    public List<String> actions() {
        return Arrays.stream(AuditAction.values()).map(Enum::name).toList();
    }
}
