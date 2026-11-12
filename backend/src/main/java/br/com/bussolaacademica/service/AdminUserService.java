package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AdminUserDetailResponse;
import br.com.bussolaacademica.dto.AdminUserResponse;
import br.com.bussolaacademica.dto.CreateAdminRequest;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.dto.ResultSummaryResponse;
import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.exception.EmailAlreadyUsedException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Regras da gestão de usuários pelo administrador (RF12).
 * <ul>
 *   <li>Lista paginada, com busca por nome ou e-mail e filtro por perfil.</li>
 *   <li>O administrador pode cadastrar outro administrador e promover ou rebaixar usuários.</li>
 *   <li>Ninguém altera o próprio perfil: assim a plataforma nunca fica sem administrador.</li>
 *   <li>O administrador pode excluir a conta de outra pessoa (cadastro e resultados); a própria conta
 *       só é excluída pelo titular, em "Meus dados".</li>
 *   <li>O administrador consulta o cadastro e o histórico de testes de um usuário (suporte ao estudante);
 *       cada consulta fica registrada na auditoria, porque é acesso a dado pessoal de outra pessoa.</li>
 *   <li>Toda criação e mudança de perfil é registrada na auditoria.</li>
 * </ul>
 */
@Service
public class AdminUserService {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final VocationalTestResultRepository resultRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    private final Clock clock;
    private final TermsService termsService;

    public AdminUserService(UserRepository userRepository, VocationalTestResultRepository resultRepository,
                            PasswordEncoder passwordEncoder, CurrentUser currentUser, AuditService auditService, Clock clock,
                            TermsService termsService) {
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = currentUser;
        this.auditService = auditService;
        this.clock = clock;
        this.termsService = termsService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> search(String text, Role role, int page, int size) {
        Specification<User> filter = Specification.where(null);
        if (text != null && !text.isBlank()) {
            String pattern = "%" + text.trim().toLowerCase(Locale.ROOT) + "%";
            filter = filter.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)));
        }
        if (role != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt", "id"));
        return PageResponse.from(userRepository.findAll(filter, pageRequest).map(AdminUserResponse::from));
    }

    /** Cadastro e histórico de testes de um usuário, do mais recente para o mais antigo. A consulta é auditada. */
    @Transactional(readOnly = true)
    public AdminUserDetailResponse details(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        List<ResultSummaryResponse> results = resultRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ResultSummaryResponse::from)
                .toList();
        auditService.recordForCurrentUser(AuditAction.USER_DATA_VIEWED,
                "Consultou o cadastro e os " + results.size() + " resultado(s) de " + user.getEmail(), true);
        return AdminUserDetailResponse.from(user, results);
    }

    @Transactional
    public AdminUserResponse createAdmin(CreateAdminRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException();
        }
        User admin = userRepository.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password()),
                Role.ADMIN, LocalDateTime.now(clock), termsService.currentVersion()));
        auditService.recordForCurrentUser(AuditAction.ADMIN_CREATED, "Cadastrou o administrador " + email, true);
        return AdminUserResponse.from(admin);
    }

    @Transactional
    public AdminUserResponse changeRole(Long userId, Role newRole) {
        if (userId.equals(currentUser.id())) {
            auditService.recordForCurrentUser(AuditAction.USER_ROLE_CHANGED, "Tentou alterar o próprio perfil", false);
            throw new BusinessRuleException("Você não pode alterar o seu próprio perfil.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Role previous = user.getRole();
        user.changeRole(newRole);
        auditService.recordForCurrentUser(AuditAction.USER_ROLE_CHANGED,
                "Perfil de " + user.getEmail() + ": " + label(previous) + " -> " + label(newRole), true);
        return AdminUserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (userId.equals(currentUser.id())) {
            auditService.recordForCurrentUser(AuditAction.ACCOUNT_DELETED, "Tentou excluir a própria conta pelo painel", false);
            throw new BusinessRuleException("Para excluir a sua própria conta, use a página Meus dados.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        resultRepository.deleteByUser_Id(user.getId());
        userRepository.delete(user);
        auditService.recordForCurrentUser(AuditAction.ACCOUNT_DELETED,
                "Conta de " + user.getEmail() + " e resultados excluídos pelo administrador", true);
    }

    private static String label(Role role) {
        return role == Role.ADMIN ? "Administrador" : "Estudante";
    }
}
