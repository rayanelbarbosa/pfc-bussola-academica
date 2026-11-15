package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.ResultSummaryResponse;
import br.com.bussolaacademica.dto.UserDataExportResponse;
import br.com.bussolaacademica.dto.UserResponse;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Operações do titular sobre os próprios dados (LGPD, art. 18):
 * confirmação e acesso aos dados, exportação (portabilidade) e eliminação da conta.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final VocationalTestResultRepository resultRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    private final Clock clock;
    private final PasswordConfirmationService passwordConfirmation;

    public UserService(UserRepository userRepository, VocationalTestResultRepository resultRepository,
                       CurrentUser currentUser, AuditService auditService, Clock clock,
                       PasswordConfirmationService passwordConfirmation) {
        this.userRepository = userRepository;
        this.resultRepository = resultRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
        this.clock = clock;
        this.passwordConfirmation = passwordConfirmation;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return UserResponse.from(loadCurrent());
    }

    @Transactional(readOnly = true)
    public List<ResultSummaryResponse> myResults() {
        return resultRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.id()).stream()
                .map(ResultSummaryResponse::from)
                .toList();
    }

    /** Todos os dados pessoais do titular em um único JSON. A própria exportação fica registrada. */
    @Transactional(readOnly = true)
    public UserDataExportResponse exportMyData() {
        User user = loadCurrent();
        auditService.record(AuditAction.PERSONAL_DATA_EXPORTED, user.getId(), user.getEmail(),
                "Exportação dos dados pessoais em JSON", true);
        return new UserDataExportResponse(LocalDateTime.now(clock), UserResponse.from(user),
                resultRepository.findByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                        .map(UserDataExportResponse.ExportedTestResult::from)
                        .toList(),
                auditService.findByUser(user.getId()).stream()
                        .map(UserDataExportResponse.ExportedActivity::from)
                        .toList());
    }

    /** Confere a senha, exclui a conta e anonimiza os resultados do teste do usuário (eles deixam de ser dele). */
    @Transactional
    public void deleteMyAccount(String password) {
        User user = loadCurrent();
        passwordConfirmation.confirm(user, password);
        resultRepository.anonymizeByUser(user.getId());
        userRepository.delete(user);
        auditService.record(AuditAction.ACCOUNT_DELETED, user.getId(), user.getEmail(),
                "Conta excluída e resultados anonimizados a pedido do titular", true);
    }

    private User loadCurrent() {
        return userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }
}
