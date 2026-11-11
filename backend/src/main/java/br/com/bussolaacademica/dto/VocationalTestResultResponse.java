package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.service.CareerRecommender.Recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resultado do teste vocacional: escore de cada categoria RIASEC (na ordem R, I, A, S, E, C),
 * a(s) categoria(s) de maior escore, as áreas de interesse escolhidas, o ranking único de carreiras
 * dessas áreas e a comparação entre as áreas.
 */
public record VocationalTestResultResponse(
        Long id,
        LocalDateTime createdAt,
        List<CategoryScoreResponse> scores,
        List<CategoryScoreResponse> topCategories,
        List<OptionResponse> interestAreas,
        List<CareerRecommendationResponse> careerRecommendations,
        List<AreaRecommendationResponse> areaMatches
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
                result.getTopCategories().stream()
                        .map(category -> CategoryScoreResponse.of(category, scores.get(category)))
                        .toList(),
                result.getInterestAreas().stream()
                        .map(area -> new OptionResponse(area.name(), area.getLabel()))
                        .toList(),
                recommendation.careers().stream().map(CareerRecommendationResponse::from).toList(),
                recommendation.areaMatches().stream().map(AreaRecommendationResponse::from).toList()
        );
    }
}
