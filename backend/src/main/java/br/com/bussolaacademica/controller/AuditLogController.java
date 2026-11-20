package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AuditIntegrityResponse;
import br.com.bussolaacademica.dto.AuditLogResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.service.AuditIntegrityService;
import br.com.bussolaacademica.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Consulta dos logs de auditoria: rota /api/admin/**, liberada só para o perfil ADMIN (SecurityConfig). */
@RestController
@RequestMapping("/api/admin/audit-logs")
public class AuditLogController {

    private final AuditService auditService;
    private final AuditIntegrityService integrityService;

    public AuditLogController(AuditService auditService, AuditIntegrityService integrityService) {
        this.auditService = auditService;
        this.integrityService = integrityService;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> search(@RequestParam(required = false) AuditAction action,
                                                 @RequestParam(required = false) String email,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return auditService.search(action, email, from, to, page, size);
    }

    /**
     * Confere a corrente de hashes dos registros: mostra se algum foi alterado, apagado ou inserido por fora. A
     * verificação também fica registrada na auditoria (depois de terminar, para não esperar a própria trava).
     */
    @GetMapping("/integrity")
    public AuditIntegrityResponse integrity() {
        AuditIntegrityResponse result = integrityService.verify();
        auditService.recordForCurrentUser(AuditAction.AUDIT_INTEGRITY_CHECKED, result.message(), result.intact());
        return result;
    }

    @GetMapping("/actions")
    public List<String> actions() {
        return Arrays.stream(AuditAction.values()).map(Enum::name).toList();
    }
}
