package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.service.CareerRecommender.Recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resultado do teste vocacional: escore de cada categoria RIASEC (na ordem R, I, A, S, E, C) e a pontuação máxima
 * de cada uma ({@code maxScore}: 20 no questionário atual, 10 nos resultados antigos), se o teste foi respondido sem
 * variação ({@code noVariation}: os 6 tipos com a mesma pontuação, então o resultado não diferencia os interesses),
 * a(s) categoria(s) de maior escore, as áreas de interesse escolhidas, as 4 carreiras recomendadas dessas áreas e,
 * quando nenhuma carreira delas tem o perfil do estudante como tipo principal (aviso de área), as 4 que mais combinam
 * com ele em todas as áreas ({@code suggestedCareers}, vazia quando não há aviso) e o ranking completo de todas as
 * carreiras ({@code allCareers}), para nenhuma ficar escondida pelos empates.
 */
public record VocationalTestResultResponse(
        Long id,
        LocalDateTime createdAt,
        List<CategoryScoreResponse> scores,
        int maxScore,
        boolean noVariation,
        List<CategoryScoreResponse> topCategories,
        List<OptionResponse> interestAreas,
        List<CareerRecommendationResponse> careerRecommendations,
        List<CareerRecommendationResponse> suggestedCareers,
        List<CareerRecommendationResponse> allCareers
) {

    public static VocationalTestResultResponse from(VocationalTestResult result,
                                                    Recommendation recommendation) {
        Map<RiasecCategory, Integer> scores = result.getScores();
        return new VocationalTestResultResponse(
                result.getId(),
                result.getCreatedAt(),
                scores.entrySet().stream()
                        .map(entry -> CategoryScoreResponse.of(entry.getKey(), entry.getValue()))
                        .toList(),
                result.getMaxScore(),
                hasNoVariation(scores),
                result.getTopCategories().stream()
                        .map(category -> CategoryScoreResponse.of(category, scores.get(category)))
                        .toList(),
                result.getInterestAreas().stream()
                        .map(area -> new OptionResponse(area.name(), area.getLabel()))
                        .toList(),
                recommendation.careers().stream().map(CareerRecommendationResponse::from).toList(),
                recommendation.suggestions().stream().map(CareerRecommendationResponse::from).toList(),
                recommendation.ranking().stream().map(CareerRecommendationResponse::from).toList()
        );
    }

    /** Regra de negócio: os 6 tipos com a mesma pontuação (ex.: "Não sei" em todas as atividades) não diferenciam nada. */
    static boolean hasNoVariation(Map<RiasecCategory, Integer> scores) {
        return scores.values().stream().distinct().count() == 1;
    }
}
