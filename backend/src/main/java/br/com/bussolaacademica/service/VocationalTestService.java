package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AnswerRequest;
import br.com.bussolaacademica.dto.OptionResponse;
import br.com.bussolaacademica.dto.QuestionResponse;
import br.com.bussolaacademica.dto.SubmitTestRequest;
import br.com.bussolaacademica.dto.VocationalTestResultResponse;
import br.com.bussolaacademica.exception.InvalidAnswersException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.Question;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.User;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.CareerRepository;
import br.com.bussolaacademica.repository.QuestionRepository;
import br.com.bussolaacademica.repository.UserRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Regra de negócio: pontuação do teste vocacional com base no modelo RIASEC (Holland).
 *
 * <ol>
 *   <li>O usuário responde TODAS as perguntas do questionário, uma única vez cada, com nota de 1 a 5.</li>
 *   <li>O escore de cada categoria é a soma das notas das perguntas daquela categoria
 *       (2 perguntas por categoria, logo de 2 a 10 pontos).</li>
 *   <li>A(s) categoria(s) com maior escore formam o perfil predominante (empates são mantidos).</li>
 *   <li>O estudante escolhe de 1 a 3 áreas de interesse ("Ainda não sei" não combina com outras);
 *       as carreiras de cada área são ranqueadas pelo {@link CareerRecommender}.</li>
 * </ol>
 * Cada resultado pertence ao usuário que respondeu: só ele (ou um administrador) pode consultá-lo.
 */
@Service
public class VocationalTestService {

    private static final int MAX_INTEREST_AREAS = 3;

    private final QuestionRepository questionRepository;
    private final CareerRepository careerRepository;
    private final CareerRecommender careerRecommender;
    private final VocationalTestResultRepository resultRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AuditService auditService;
    private final Clock clock;

    public VocationalTestService(QuestionRepository questionRepository,
                                 CareerRepository careerRepository,
                                 CareerRecommender careerRecommender,
                                 VocationalTestResultRepository resultRepository,
                                 UserRepository userRepository,
                                 CurrentUser currentUser,
                                 AuditService auditService,
                                 Clock clock) {
        this.questionRepository = questionRepository;
        this.careerRepository = careerRepository;
        this.careerRecommender = careerRecommender;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions() {
        return questionRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(QuestionResponse::from)
                .toList();
    }

    public List<OptionResponse> listInterestAreas() {
        return Arrays.stream(InterestArea.values())
                .map(area -> new OptionResponse(area.name(), area.getLabel()))
                .toList();
    }

    @Transactional
    public VocationalTestResultResponse submit(SubmitTestRequest request) {
        List<Question> questions = questionRepository.findAllByOrderByDisplayOrderAsc();
        validateAnswers(request.answers(), questions);
        List<InterestArea> interestAreas = normalizeInterestAreas(request.interestAreas());

        Map<RiasecCategory, Integer> scores = calculateScores(request.answers(), questions);
        List<RiasecCategory> topCategories = findTopCategories(scores);

        User owner = userRepository.findById(currentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        VocationalTestResult result = resultRepository.save(
                new VocationalTestResult(owner, LocalDateTime.now(clock), scores, topCategories, interestAreas));
        auditService.record(AuditAction.TEST_SUBMITTED, owner.getId(), owner.getEmail(),
                "resultadoId=" + result.getId() + ", perfil=" + topCategories + ", areas=" + interestAreas, true);
        return toResponse(result);
    }

    @Transactional(readOnly = true)
    public VocationalTestResultResponse findResult(Long id) {
        VocationalTestResult result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado não encontrado: id=" + id));
        if (!currentUser.isAdmin() && !result.belongsTo(currentUser.id())) {
            throw new AccessDeniedException("Você só pode consultar os seus próprios resultados (resultado " + id + ").");
        }
        return toResponse(result);
    }

    private VocationalTestResultResponse toResponse(VocationalTestResult result) {
        return VocationalTestResultResponse.from(result, careerRecommender.recommend(
                result.getInterestAreas(), careerRepository.findAll(), result.getScores()));
    }

    /** Sem áreas = "Ainda não sei"; remove repetidas; no máximo 3; "Ainda não sei" sozinha. */
    private List<InterestArea> normalizeInterestAreas(List<InterestArea> requested) {
        if (requested == null || requested.isEmpty()) {
            return List.of(InterestArea.UNDECIDED);
        }
        if (requested.stream().anyMatch(java.util.Objects::isNull)) {
            throw new InvalidAnswersException("Área de interesse inválida");
        }
        List<InterestArea> areas = List.copyOf(new LinkedHashSet<>(requested));
        if (areas.size() > MAX_INTEREST_AREAS) {
            throw new InvalidAnswersException("Escolha no máximo 3 áreas de interesse");
        }
        if (areas.contains(InterestArea.UNDECIDED) && areas.size() > 1) {
            throw new InvalidAnswersException("\"Ainda não sei\" não pode ser combinada com outras áreas");
        }
        return areas;
    }

    private void validateAnswers(List<AnswerRequest> answers, List<Question> questions) {
        Set<Integer> validIds = questions.stream().map(Question::getId).collect(Collectors.toSet());
        Set<Integer> answered = new HashSet<>();

        for (AnswerRequest answer : answers) {
            if (!validIds.contains(answer.questionId())) {
                throw new InvalidAnswersException("Pergunta inexistente: id=" + answer.questionId());
            }
            if (!answered.add(answer.questionId())) {
                throw new InvalidAnswersException("Pergunta respondida mais de uma vez: id=" + answer.questionId());
            }
        }

        if (answered.size() != validIds.size()) {
            List<Integer> missing = new ArrayList<>(validIds);
            missing.removeAll(answered);
            Collections.sort(missing);
            throw new InvalidAnswersException("Responda todas as perguntas. Faltando: " + missing);
        }
    }

    private Map<RiasecCategory, Integer> calculateScores(List<AnswerRequest> answers, List<Question> questions) {
        Map<Integer, Question> questionsById = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        for (RiasecCategory category : RiasecCategory.values()) {
            scores.put(category, 0);
        }
        for (AnswerRequest answer : answers) {
            RiasecCategory category = questionsById.get(answer.questionId()).getCategory();
            scores.merge(category, answer.score(), Integer::sum);
        }
        return scores;
    }

    private List<RiasecCategory> findTopCategories(Map<RiasecCategory, Integer> scores) {
        int highest = Collections.max(scores.values());
        return scores.entrySet().stream()
                .filter(entry -> entry.getValue() == highest)
                .map(Map.Entry::getKey)
                .toList();
    }

}
