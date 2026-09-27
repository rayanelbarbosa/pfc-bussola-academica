package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AnswerRequest;
import br.com.bussolaacademica.dto.CategoryScoreResponse;
import br.com.bussolaacademica.dto.SubmitTestRequest;
import br.com.bussolaacademica.dto.VocationalTestResultResponse;
import br.com.bussolaacademica.exception.InvalidAnswersException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.Question;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.QuestionRepository;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@ExtendWith(MockitoExtension.class)
class VocationalTestServiceTest {

    /** Mesmo questionário do banco: perguntas 1-2 Realista, 3-4 Investigativo ... 11-12 Convencional. */
    private static final List<Question> QUESTIONS = IntStream.rangeClosed(1, 12)
            .mapToObj(id -> new Question(id, "Pergunta " + id, RiasecCategory.values()[(id - 1) / 2], id))
            .toList();

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private VocationalTestResultRepository resultRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUser currentUser;

    @Mock
    private AuditService auditService;

    private VocationalTestService service;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-09-28T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new VocationalTestService(questionRepository, resultRepository, userRepository,
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

        // Realista 3+3=6, Investigativo 5+4=9, Artístico 4+3=7, Social 4+4=8, Empreendedor 2+3=5, Convencional 2+2=4
        VocationalTestResultResponse result = service.submit(request(3, 3, 5, 4, 4, 3, 4, 4, 2, 3, 2, 2));

        assertThat(result.scores()).extracting(CategoryScoreResponse::label, CategoryScoreResponse::score)
                .containsExactly(
                        tuple("Realista", 6),
                        tuple("Investigativo", 9),
                        tuple("Artístico", 7),
                        tuple("Social", 8),
                        tuple("Empreendedor", 5),
                        tuple("Convencional", 4));
        assertThat(result.topCategories()).extracting(CategoryScoreResponse::category).containsExactly("INVESTIGATIVE");
        assertThat(result.recommendedAreas()).containsExactly("Ciências Exatas", "Ciências Biológicas", "Medicina");
        assertThat(result.createdAt()).isEqualTo("2026-09-28T09:00:00");
        verify(resultRepository).save(any(VocationalTestResult.class));
    }

    @Test
    void shouldKeepAllTiedCategoriesAndMergeTheirAreas() {
        loggedAs(1L);
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        when(resultRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Social 5+5=10 e Empreendedor 5+5=10 empatados no topo
        VocationalTestResultResponse result = service.submit(request(1, 1, 2, 2, 3, 3, 5, 5, 5, 5, 1, 1));

        assertThat(result.topCategories()).extracting(CategoryScoreResponse::label)
                .containsExactly("Social", "Empreendedor");
        assertThat(result.recommendedAreas()).containsExactly(
                "Pedagogia", "Psicologia", "Enfermagem", "Administração", "Direito", "Economia");
    }

    @Test
    void shouldRejectIncompleteQuestionnaire() {
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        SubmitTestRequest incomplete = new SubmitTestRequest(List.of(new AnswerRequest(1, 5)));

        assertThatThrownBy(() -> service.submit(incomplete))
                .isInstanceOf(InvalidAnswersException.class)
                .hasMessageContaining("Faltando: [2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]");
        verify(resultRepository, never()).save(any());
    }

    @Test
    void shouldRejectQuestionAnsweredTwice() {
        when(questionRepository.findAllByOrderByDisplayOrderAsc()).thenReturn(QUESTIONS);
        List<AnswerRequest> answers = new ArrayList<>(request(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3).answers());
        answers.set(11, new AnswerRequest(1, 5));

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

        assertThat(service.listQuestions()).hasSize(12)
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

        assertThat(service.findResult(10L).recommendedAreas()).contains("Medicina");
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

    private static VocationalTestResult savedResultOf(User owner) {
        java.util.Map<RiasecCategory, Integer> scores = new java.util.EnumMap<>(RiasecCategory.class);
        for (RiasecCategory category : RiasecCategory.values()) {
            scores.put(category, category == RiasecCategory.INVESTIGATIVE ? 9 : 5);
        }
        return new VocationalTestResult(owner, LocalDateTime.of(2026, 9, 28, 9, 0), scores,
                List.of(RiasecCategory.INVESTIGATIVE), RiasecCategory.INVESTIGATIVE.getRecommendedAreas());
    }

    private static SubmitTestRequest request(int... scores) {
        List<AnswerRequest> answers = IntStream.range(0, scores.length)
                .mapToObj(i -> new AnswerRequest(i + 1, scores[i]))
                .toList();
        return new SubmitTestRequest(answers);
    }
}
