package br.com.bussolaacademica.service;

import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.exception.TooManyLoginAttemptsException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Senha pedida de novo nas ações críticas, com o mesmo bloqueio do login contra força bruta. */
@ExtendWith(MockitoExtension.class)
class PasswordConfirmationServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditService auditService;

    private PasswordConfirmationService service;
    private User admin;

    @BeforeEach
    void setUp() {
        service = new PasswordConfirmationService(passwordEncoder, auditService);
        admin = new User("Admin", "admin@email.com", "hash-da-senha", Role.ADMIN, LocalDateTime.of(2026, 9, 1, 10, 0), "1.0");
        ReflectionTestUtils.setField(admin, "id", 7L);
    }

    @Test
    void deveAceitarASenhaCorretaSemRegistrarFalha() {
        when(passwordEncoder.matches("Senha1234", "hash-da-senha")).thenReturn(true);

        assertDoesNotThrow(() -> service.confirm(admin, "Senha1234"));

        verify(auditService, never()).record(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void deveRecusarASenhaErradaERegistrarComoTentativaSemSucesso() {
        when(passwordEncoder.matches("errada", "hash-da-senha")).thenReturn(false);

        assertThrows(BusinessRuleException.class, () -> service.confirm(admin, "errada"));

        verify(auditService).record(eq(AuditAction.LOGIN_FAILURE), eq(7L), eq("admin@email.com"), anyString(), eq(false));
    }

    @Test
    void deveRecusarSenhaVaziaSemConsultarOHash() {
        assertThrows(BusinessRuleException.class, () -> service.confirm(admin, null));

        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void deveBloquearDepoisDeCincoTentativasSemSucesso() {
        when(auditService.countLoginFailuresSinceLastSuccess("admin@email.com", AuthService.BLOCK_WINDOW))
                .thenReturn((long) AuthService.MAX_FAILED_ATTEMPTS);

        assertThrows(TooManyLoginAttemptsException.class, () -> service.confirm(admin, "Senha1234"));

        verify(passwordEncoder, never()).matches(any(), any());
        verify(auditService).record(eq(AuditAction.LOGIN_BLOCKED), eq(7L), eq("admin@email.com"), anyString(), eq(false));
    }
}
