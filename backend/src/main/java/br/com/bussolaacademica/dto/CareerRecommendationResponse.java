package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.service.CareerRecommender.RankedCareer;

import java.util.List;

/** Carreira recomendada: código RIASEC (ex.: "RI"), tipos do código e pontuação de compatibilidade. */
public record CareerRecommendationResponse(String name, String code, List<CategoryLabel> categories,
                                           int compatibility, String description) {

    public record CategoryLabel(String category, String label) {
    }

    public static CareerRecommendationResponse from(RankedCareer ranked) {
        var career = ranked.career();
        return new CareerRecommendationResponse(career.getName(), career.getCode(),
                List.of(new CategoryLabel(career.getPrimaryCategory().name(), career.getPrimaryCategory().getLabel()),
                        new CategoryLabel(career.getSecondaryCategory().name(), career.getSecondaryCategory().getLabel())),
                ranked.compatibility(), career.getDescription());
    }
}
