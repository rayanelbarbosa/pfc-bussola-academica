package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AnswerRequest;
import br.com.bussolaacademica.dto.QuestionResponse;
import br.com.bussolaacademica.dto.SubmitTestRequest;
import br.com.bussolaacademica.dto.VocationalTestResultResponse;
import br.com.bussolaacademica.exception.InvalidAnswersException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.Question;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.repository.QuestionRepository;
import br.com.bussolaacademica.repository.VocationalTestResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
 *   <li>As áreas recomendadas são a união das áreas das categorias predominantes.</li>
 * </ol>
 */
@Service
public class VocationalTestService {

    private final QuestionRepository questionRepository;
    private final VocationalTestResultRepository resultRepository;
    private final Clock clock;

    public VocationalTestService(QuestionRepository questionRepository,
                                 VocationalTestResultRepository resultRepository,
                                 Clock clock) {
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponse> listQuestions() {
        return questionRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(QuestionResponse::from)
                .toList();
    }

    @Transactional
    public VocationalTestResultResponse submit(SubmitTestRequest request) {
        List<Question> questions = questionRepository.findAllByOrderByDisplayOrderAsc();
        validateAnswers(request.answers(), questions);

        Map<RiasecCategory, Integer> scores = calculateScores(request.answers(), questions);
        List<RiasecCategory> topCategories = findTopCategories(scores);
        List<String> areas = mergeRecommendedAreas(topCategories);

        VocationalTestResult result = new VocationalTestResult(LocalDateTime.now(clock), scores, topCategories, areas);
        return VocationalTestResultResponse.from(resultRepository.save(result));
    }

    @Transactional(readOnly = true)
    public VocationalTestResultResponse findResult(Long id) {
        return resultRepository.findById(id)
                .map(VocationalTestResultResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Resultado não encontrado: id=" + id));
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

    private List<String> mergeRecommendedAreas(List<RiasecCategory> topCategories) {
        Set<String> areas = new LinkedHashSet<>();
        topCategories.forEach(category -> areas.addAll(category.getRecommendedAreas()));
        return List.copyOf(areas);
    }
}
