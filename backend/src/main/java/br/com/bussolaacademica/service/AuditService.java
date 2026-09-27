package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AuditLogResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.AuditLog;
import br.com.bussolaacademica.repository.AuditLogRepository;
import br.com.bussolaacademica.security.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Registro e consulta dos logs de auditoria.
 *
 * Cada registro é gravado numa transação própria (REQUIRES_NEW): assim uma tentativa de login
 * com senha errada ou um acesso negado ficam registrados mesmo quando a operação principal falha.
 */
@Service
public class AuditService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_DETAILS_LENGTH = 500;

    private final AuditLogRepository repository;
    private final CurrentUser currentUser;
    private final Clock clock;

    public AuditService(AuditLogRepository repository, CurrentUser currentUser, Clock clock) {
        this.repository = repository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(AuditAction action, Long userId, String userEmail, String details, boolean success) {
        HttpServletRequest request = currentRequest();
        String resource = request == null ? null : request.getMethod() + " " + request.getRequestURI();
        repository.save(new AuditLog(LocalDateTime.now(clock), userId, userEmail, action, resource,
                truncate(details), clientIp(request), success));
    }

    /** Registra a ação em nome do usuário autenticado na requisição atual (se houver). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordForCurrentUser(AuditAction action, String details, boolean success) {
        record(action, currentUser.idIfAuthenticated().orElse(null),
                currentUser.emailIfAuthenticated().orElse(null), details, success);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(AuditAction action, String email, int page, int size) {
        Specification<AuditLog> filter = Specification.where(null);
        if (action != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("action"), action));
        }
        if (email != null && !email.isBlank()) {
            String pattern = "%" + email.trim().toLowerCase() + "%";
            filter = filter.and((root, query, cb) -> cb.like(cb.lower(root.<String>get("userEmail")), pattern));
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return PageResponse.from(repository.findAll(filter, pageRequest).map(AuditLogResponse::from));
    }

    private static HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    /** IP do cliente. Atrás do nginx, o IP real vem no cabeçalho X-Forwarded-For. */
    private static String clientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        return ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip;
    }

    private static String truncate(String details) {
        return details != null && details.length() > MAX_DETAILS_LENGTH ? details.substring(0, MAX_DETAILS_LENGTH) : details;
    }
}
