package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.service.CareerRecommender.AreaMatch;

/**
 * Comparação entre as áreas consideradas: a melhor carreira de cada área e a compatibilidade dela.
 * Vem ordenada da área que mais combina com o estudante para a que menos combina.
 */
public record AreaRecommendationResponse(String area, String label, String bestCareer, int bestCompatibility) {

    public static AreaRecommendationResponse from(AreaMatch match) {
        return new AreaRecommendationResponse(match.area().name(), match.area().getLabel(),
                match.best().career().getName(), match.best().compatibility());
    }
}
