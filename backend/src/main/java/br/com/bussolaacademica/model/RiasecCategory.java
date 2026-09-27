package br.com.bussolaacademica.model;

import java.util.List;

/**
 * As seis categorias do modelo vocacional RIASEC (Holland, 1959), base da regra de
 * negócio de pontuação do teste vocacional. Cada categoria tem um rótulo em português
 * (exibido ao usuário) e as áreas recomendadas quando ela é o perfil predominante.
 */
public enum RiasecCategory {

    REALISTIC("Realista", List.of("Engenharias", "Tecnologia da Informação", "Agronomia")),
    INVESTIGATIVE("Investigativo", List.of("Ciências Exatas", "Ciências Biológicas", "Medicina")),
    ARTISTIC("Artístico", List.of("Design", "Arquitetura e Urbanismo", "Comunicação Social")),
    SOCIAL("Social", List.of("Pedagogia", "Psicologia", "Enfermagem")),
    ENTERPRISING("Empreendedor", List.of("Administração", "Direito", "Economia")),
    CONVENTIONAL("Convencional", List.of("Ciências Contábeis", "Gestão Pública", "Biblioteconomia"));

    private final String label;
    private final List<String> recommendedAreas;

    RiasecCategory(String label, List<String> recommendedAreas) {
        this.label = label;
        this.recommendedAreas = recommendedAreas;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getRecommendedAreas() {
        return recommendedAreas;
    }
}
