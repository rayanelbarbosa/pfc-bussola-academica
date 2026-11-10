package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.service.CareerRecommender.RankedCareer;

import java.util.ArrayList;
import java.util.List;

/** Carreira recomendada: código RIASEC (ex.: "RIC"), tipos do código e pontuação de compatibilidade. */
public record CareerRecommendationResponse(String name, String code, List<CategoryLabel> categories,
                                           int compatibility, String description) {

    public record CategoryLabel(String category, String label) {
    }

    public static CareerRecommendationResponse from(RankedCareer ranked) {
        var career = ranked.career();
        List<CategoryLabel> categories = new ArrayList<>(List.of(
                new CategoryLabel(career.getPrimaryCategory().name(), career.getPrimaryCategory().getLabel()),
                new CategoryLabel(career.getSecondaryCategory().name(), career.getSecondaryCategory().getLabel())));
        if (career.getTertiaryCategory() != null) {
            categories.add(new CategoryLabel(career.getTertiaryCategory().name(), career.getTertiaryCategory().getLabel()));
        }
        return new CareerRecommendationResponse(career.getName(), career.getCode(), List.copyOf(categories),
                ranked.compatibility(), career.getDescription());
    }
}
