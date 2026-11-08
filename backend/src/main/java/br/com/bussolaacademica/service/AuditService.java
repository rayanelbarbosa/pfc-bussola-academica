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
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

/**
 * Registro e consulta dos logs de auditoria.
 *
 * Cada registro é gravado numa transação própria (REQUIRES_NEW): assim uma tentativa de login
 * com senha errada ou um acesso negado ficam registrados mesmo quando a operação principal falha.
 *
 * Só ações de segurança, alterações de dados e ações administrativas são registradas (consultas
 * rotineiras não), e os registros são guardados por 6 meses (Marco Civil da Internet, art. 15).
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

    /**
     * Senhas erradas para o e-mail dentro da janela, contadas só depois do último login com sucesso
     * (quem acerta a senha zera a contagem).
     */
    @Transactional(readOnly = true)
    public long countLoginFailuresSinceLastSuccess(String email, Duration window) {
        LocalDateTime since = LocalDateTime.now(clock).minus(window);
        LocalDateTime lastSuccess = repository
                .findFirstByUserEmailAndActionOrderByCreatedAtDesc(email, AuditAction.LOGIN_SUCCESS)
                .map(AuditLog::getCreatedAt)
                .orElse(null);
        if (lastSuccess != null && lastSuccess.isAfter(since)) {
            since = lastSuccess;
        }
        return repository.countByUserEmailAndActionAndCreatedAtAfter(email, AuditAction.LOGIN_FAILURE, since);
    }

    /** Histórico de ações do próprio titular (usado na exportação dos dados pessoais). */
    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByUser(Long userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(AuditLogResponse::from).toList();
    }

    /** Expurgo: apaga os registros mais antigos que o prazo de guarda e registra quantos foram apagados. */
    @Transactional
    public int purgeOlderThan(Period retention) {
        LocalDateTime limit = LocalDateTime.now(clock).minus(retention);
        int deleted = repository.deleteOlderThan(limit);
        if (deleted > 0) {
            repository.save(new AuditLog(LocalDateTime.now(clock), null, null, AuditAction.AUDIT_LOGS_PURGED,
                    null, deleted + " registros anteriores a " + limit.toLocalDate() + " apagados", null, true));
        }
        return deleted;
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
