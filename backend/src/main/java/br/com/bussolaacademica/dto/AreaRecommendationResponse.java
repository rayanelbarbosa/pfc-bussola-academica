package br.com.bussolaacademica.dto;

import java.util.List;

/** Carreiras recomendadas dentro de uma área de interesse escolhida. */
public record AreaRecommendationResponse(String area, String label, List<CareerRecommendationResponse> careers) {
}
