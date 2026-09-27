package br.com.bussolaacademica.dto;

import br.com.bussolaacademica.model.RiasecCategory;

/** Escore de uma categoria RIASEC: código (ex.: INVESTIGATIVE), rótulo em português e pontuação (2 a 10). */
public record CategoryScoreResponse(String category, String label, int score) {

    public static CategoryScoreResponse of(RiasecCategory category, int score) {
        return new CategoryScoreResponse(category.name(), category.getLabel(), score);
    }
}
