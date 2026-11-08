package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;
import br.com.bussolaacademica.service.CareerRecommender.RankedCareer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resultado do teste vocacional: escore de cada categoria RIASEC (na ordem R, I, A, S, E, C),
 * a(s) categoria(s) de maior escore, as áreas de interesse escolhidas e as carreiras
 * recomendadas em cada uma delas.
 */
public record VocationalTestResultResponse(
        Long id,
        LocalDateTime createdAt,
        List<CategoryScoreResponse> scores,
        List<CategoryScoreResponse> topCategories,
        List<OptionResponse> interestAreas,
        List<AreaRecommendationResponse> careerRecommendations
) {

    public static VocationalTestResultResponse from(VocationalTestResult result,
                                                    Map<InterestArea, List<RankedCareer>> careers) {
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
                careers.entrySet().stream()
                        .map(entry -> new AreaRecommendationResponse(entry.getKey().name(),
                                entry.getKey() == InterestArea.UNDECIDED ? "Todas as áreas" : entry.getKey().getLabel(),
                                entry.getValue().stream().map(CareerRecommendationResponse::from).toList()))
                        .toList()
        );
    }
}
