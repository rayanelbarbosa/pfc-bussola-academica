package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.ResultSummaryResponse;
import br.com.bussolaacademica.dto.UserResponse;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Testes unitários da conta do usuário: consulta, histórico e exclusão (LGPD). */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private VocationalTestResultRepository resultRepository;
    @Mock
    private CurrentUser currentUser;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private static User aluna() {
        User user = new User("Aluna", "aluna@email.com", "hash", Role.STUDENT,
                LocalDateTime.of(2026, 9, 1, 10, 0), "1.0");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    @Test
    void deveRetornarDadosDoUsuarioLogado() {
        // Arrange
        when(currentUser.id()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(aluna()));

        // Act
        UserResponse response = userService.me();

        // Assert
        assertEquals("aluna@email.com", response.email());
        assertEquals("STUDENT", response.role());
        assertEquals("1.0", response.termsVersion());
    }

    @Test
    void deveExcluirResultadosAntesDaContaERegistrarAuditoria() {
        // Arrange
        User aluna = aluna();
        when(currentUser.id()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(aluna));

        // Act
        userService.deleteMyAccount();

        // Assert
        InOrder ordem = inOrder(resultRepository, userRepository);
        ordem.verify(resultRepository).deleteByUser_Id(1L);
        ordem.verify(userRepository).delete(aluna);
        verify(auditService).record(eq(AuditAction.ACCOUNT_DELETED), eq(1L), eq("aluna@email.com"),
                anyString(), eq(true));
    }

    @Test
    void deveLancarExcecaoAoExcluirContaDeUsuarioInexistente() {
        // Arrange
        when(currentUser.id()).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteMyAccount());

        // Assert
        assertEquals("Usuário não encontrado", ex.getMessage());
        verify(resultRepository, never()).deleteByUser_Id(anyLong());
        verify(userRepository, never()).delete(any());
        verify(auditService, never()).record(any(), any(), any(), any(), anyBoolean());
    }

    @Test
    void deveRetornarHistoricoVazioQuandoUsuarioNaoFezNenhumTeste() {
        // Arrange
        when(currentUser.id()).thenReturn(1L);
        when(resultRepository.findByUser_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        // Act
        List<ResultSummaryResponse> historico = userService.myResults();

        // Assert
        assertTrue(historico.isEmpty());
        verify(auditService).recordForCurrentUser(AuditAction.PERSONAL_DATA_VIEWED, "Histórico de resultados", true);
    }
}
