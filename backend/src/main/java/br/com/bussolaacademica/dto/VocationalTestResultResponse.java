package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.model.VocationalTestResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Resultado do teste vocacional: escore de cada categoria RIASEC (na ordem R, I, A, S, E, C),
 * a(s) categoria(s) de maior escore e as áreas recomendadas.
 */
public record VocationalTestResultResponse(
        Long id,
        LocalDateTime createdAt,
        List<CategoryScoreResponse> scores,
        List<CategoryScoreResponse> topCategories,
        List<String> recommendedAreas
) {

    public static VocationalTestResultResponse from(VocationalTestResult result) {
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
                result.getRecommendedAreas()
        );
    }
}
