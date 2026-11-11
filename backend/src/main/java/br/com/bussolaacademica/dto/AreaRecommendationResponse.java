package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.dto.CareerRecommendationResponse.CategoryLabel;
import br.com.bussolaacademica.service.CareerRecommender.AreaMatch;

import java.util.List;

/**
 * Comparação entre as áreas consideradas: a melhor carreira de cada área e a compatibilidade dela.
 * Vem ordenada da área que mais combina com o estudante para a que menos combina. Os 2 tipos da melhor carreira
 * permitem explicar o porquê (ex.: "Medicina pede Investigativo e Social, seus tipos mais fortes").
 */
public record AreaRecommendationResponse(String area, String label, String bestCareer, int bestCompatibility,
                                         List<CategoryLabel> bestCategories) {

    public static AreaRecommendationResponse from(AreaMatch match) {
        var career = match.best().career();
        return new AreaRecommendationResponse(match.area().name(), match.area().getLabel(),
                career.getName(), match.best().compatibility(), List.of(
                        new CategoryLabel(career.getPrimaryCategory().name(), career.getPrimaryCategory().getLabel()),
                        new CategoryLabel(career.getSecondaryCategory().name(), career.getSecondaryCategory().getLabel())));
    }
}
