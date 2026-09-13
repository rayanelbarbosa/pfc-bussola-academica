package br.com.bussolaacademica.testevocacional;

/**
 * Representa uma pergunta fixa do questionario vocacional.
 * Cada pergunta pertence a exatamente uma categoria RIASEC.
 */
public record Pergunta(int id, String enunciado, CategoriaRiasec categoria) {
}
