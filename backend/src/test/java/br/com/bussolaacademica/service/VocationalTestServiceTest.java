package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AnswerRequest;
import br.com.bussolaacademica.dto.CategoryScoreResponse;
import br.com.bussolaacademica.dto.SubmitTestRequest;
import br.com.bussolaacademica.dto.VocationalTestResultResponse;
import br.com.bussolaacademica.exception.InvalidAnswersException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Question;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.CareerRepository;
import br.com.bussolaacademica.repository.QuestionRepository;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class VocationalTestServiceTest {

    /** Mesmo questionário do banco (Mini-IP): 30 atividades em blocos de 6 (R, I, A, S, E, C). */
    private static final List<Question> QUESTIONS = IntStream.rangeClosed(1, 30)
            .mapToObj(id -> new Question(id, "Atividade " + id, RiasecCategory.values()[(id - 1) % 6], id))
            .toList();

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private VocationalTestResultRepository resultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private AuditService auditService;

    private VocationalTestService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-09-28T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new VocationalTestService(questionRepository, careerRepository, new CareerRecommender(),
                resultRepository, userRepository,
                currentUser, auditService, fixedClock);
    }

    private static User user(long id) {
        User user = new User("Aluna " + id, "aluna" + id + "@teste.com", "hash", Role.STUDENT,
                LocalDateTime.of(2026, 9, 1, 10, 0), "1.0");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private void loggedAs(long id) {
        when(currentUser.id()).thenReturn(id);
        when(userRepository.findById(id)).thenReturn(Optional.of(user(id)));
    }

    @Test
    void shouldSumScoresPerCategoryAndFindTopProfile() {
        loggedAs(1L);
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Exemplo da Ana: cada tipo é a soma das 5 respostas dele (0 a 4 cada)
        VocationalTestResultResponse result = service.submit(byType(
                new int[] {1, 0, 1, 0, 2},   // Realista 4
                new int[] {2, 2, 3, 1, 2},   // Investigativo 10
                new int[] {3, 3, 2, 3, 3},   // Artístico 14
                new int[] {4, 3, 4, 3, 3},   // Social 17
                new int[] {2, 1, 2, 1, 2},   // Empreendedor 8
                new int[] {1, 1, 1, 1, 1})); // Convencional 5

        assertThat(result.scores()).extracting(CategoryScoreResponse::label, CategoryScoreResponse::score)
                .containsExactly(
                        tuple("Realista", 4),
                        tuple("Investigativo", 10),
                        tuple("Artístico", 14),
                        tuple("Social", 17),
                        tuple("Empreendedor", 8),
                        tuple("Convencional", 5));
        assertThat(result.maxScore()).isEqualTo(20);
        assertThat(result.noVariation()).isFalse();
        assertThat(result.topCategories()).extracting(CategoryScoreResponse::category).containsExactly("SOCIAL");
        assertThat(result.createdAt()).isEqualTo("2026-09-28T09:00:00");
        verify(resultRepository).save(any(VocationalTestResult.class));
    }

    @Test
    void shouldKeepAllTiedCategories() {
        loggedAs(1L);
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Social 20 e Empreendedor 20 empatados no topo
        VocationalTestResultResponse result = service.submit(sameAnswerPerType(0, 1, 2, 4, 4, 0));

        assertThat(result.topCategories()).extracting(CategoryScoreResponse::label)
                .containsExactly("Social", "Empreendedor");
    }

    @Test
    void shouldRejectIncompleteQuestionnaire() {
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        SubmitTestRequest incomplete = new SubmitTestRequest(List.of(new AnswerRequest(1, 4)));

        assertThatThrownBy(() -> service.submit(incomplete))
                .isInstanceOf(InvalidAnswersException.class)
                .hasMessageContaining("Faltando: [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13");
        verify(resultRepository, never()).save(any());
    }

    @Test
    void shouldRejectQuestionAnsweredTwice() {
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        List<AnswerRequest> answers = new ArrayList<>(sameAnswerPerType(3, 3, 3, 3, 3, 3).answers());
        answers.set(29, new AnswerRequest(1, 4));

        assertThatThrownBy(() -> service.submit(new SubmitTestRequest(answers)))
                .isInstanceOf(InvalidAnswersException.class)
                .hasMessageContaining("mais de uma vez: id=1");
    }

    @Test
    void shouldRejectUnknownQuestion() {
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        SubmitTestRequest request = new SubmitTestRequest(List.of(new AnswerRequest(99, 3)));

        assertThatThrownBy(() -> service.submit(request))
                .isInstanceOf(InvalidAnswersException.class)
                .hasMessageContaining("inexistente: id=99");
    }

    @Test
    void shouldListQuestionsInOrder() {
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);

        assertThat(service.listQuestions()).hasSize(30)
                .first().satisfies(q -> {
                    assertThat(q.id()).isEqualTo(1);
                    assertThat(q.category()).isEqualTo("REALISTIC");
                });
    }

    @Test
    void shouldThrowWhenResultDoesNotExist() {
        when(resultRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findResult(42L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldLetOwnerSeeTheirResult() {
        VocationalTestResult result = savedResultOf(user(1L));
        when(resultRepository.findById(10L)).thenReturn(Optional.of(result));
        when(currentUser.isAdmin()).thenReturn(false);
        when(currentUser.id()).thenReturn(1L);

        assertThat(service.findResult(10L).topCategories()).extracting(CategoryScoreResponse::category)
                .containsExactly("INVESTIGATIVE");
    }

    @Test
    void shouldDenyStudentFromSeeingSomeoneElsesResult() {
        VocationalTestResult result = savedResultOf(user(1L));
        when(resultRepository.findById(10L)).thenReturn(Optional.of(result));
        when(currentUser.isAdmin()).thenReturn(false);
        when(currentUser.id()).thenReturn(2L);

        assertThatThrownBy(() -> service.findResult(10L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void shouldLetAdminSeeAnyResult() {
        VocationalTestResult result = savedResultOf(user(1L));
        when(resultRepository.findById(10L)).thenReturn(Optional.of(result));
        when(currentUser.isAdmin()).thenReturn(true);

        assertThat(service.findResult(10L).topCategories()).isNotEmpty();
    }

    @Test
    void deveSalvarResultadoDoUsuarioLogadoERegistrarAuditoria() {
        // Arrange
        loggedAs(1L);
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act: Convencional 5 × 4 = 20 é o maior escore
        VocationalTestResultResponse result = service.submit(sameAnswerPerType(2, 3, 1, 3, 2, 4));

        // Assert
        ArgumentCaptor<VocationalTestResult> salvo = ArgumentCaptor.forClass(VocationalTestResult.class);
        verify(resultRepository).save(salvo.capture());
        assertEquals(1L, salvo.getValue().getUser().getId());
        assertThat(result.topCategories()).extracting(CategoryScoreResponse::category).containsExactly("CONVENTIONAL");
        verify(auditService).record(eq(AuditAction.TEST_SUBMITTED), eq(1L), eq("aluna1@teste.com"),
                anyString(), eq(true));
    }

    @Test
    void deveLancarExcecaoQuandoListaDeRespostasEstiverVazia() {
        // Arrange
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        SubmitTestRequest vazio = new SubmitTestRequest(List.of());

        // Act
        InvalidAnswersException ex = assertThrows(InvalidAnswersException.class, () -> service.submit(vazio));

        // Assert
        assertThat(ex.getMessage()).startsWith("Responda todas as perguntas. Faltando: [1, 2, 3,").endsWith("29, 30]");
        verify(resultRepository, never()).save(any());
        verify(auditService, never()).record(any(), any(), any(), any(), eq(true));
    }

    @ParameterizedTest
    @CsvSource({"0,0", "2,10", "4,20"})
    void deveCalcularEscoreNosLimitesDaEscalaEEmpatarTodasAsCategorias(int nota, int escoreEsperado) {
        // Arrange
        loggedAs(1L);
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // Act
        VocationalTestResultResponse result = service.submit(sameAnswerPerType(nota, nota, nota, nota, nota, nota));

        // Assert
        assertThat(result.scores()).extracting(CategoryScoreResponse::score).containsOnly(escoreEsperado);
        assertEquals(6, result.topCategories().size());
        // a mesma resposta em tudo: o resultado não diferencia os interesses e o estudante é avisado
        assertThat(result.noVariation()).isTrue();
    }

    private static VocationalTestResult savedResultOf(User owner) {
        java.util.Map<RiasecCategory, Integer> scores = new java.util.EnumMap<>(RiasecCategory.class);
        for (RiasecCategory category : RiasecCategory.values()) {
            scores.put(category, category == RiasecCategory.INVESTIGATIVE ? 9 : 5);
        }
        return new VocationalTestResult(owner, LocalDateTime.of(2026, 9, 28, 9, 0), scores,
                List.of(RiasecCategory.INVESTIGATIVE),
                List.of(br.com.bussolaacademica.model.InterestArea.UNDECIDED), 20);
    }

    /**
     * Respostas por tipo, na ordem R, I, A, S, E, C: answers[tipo][rodada]. A atividade da rodada r do tipo t é a
     * de id r × 6 + t + 1 (ordem oficial do Mini-IP, em blocos de 6).
     */
    private static SubmitTestRequest byType(int[]... answersByType) {
        List<AnswerRequest> answers = IntStream.range(0, 30)
                .mapToObj(i -> new AnswerRequest(i + 1, answersByType[i % 6][i / 6]))
                .toList();
        return new SubmitTestRequest(answers);
    }

    /** A mesma resposta nas 5 atividades de cada tipo (escore do tipo = 5 × resposta). */
    private static SubmitTestRequest sameAnswerPerType(int r, int i, int a, int s, int e, int c) {
        int[][] answers = {new int[5], new int[5], new int[5], new int[5], new int[5], new int[5]};
        int[] values = {r, i, a, s, e, c};
        for (int type = 0; type < 6; type++) {
            java.util.Arrays.fill(answers[type], values[type]);
        }
        return byType(answers);
    }
}
