package br.com.bussolaacademica.testevocacional;

import java.util.List;

/**
 * As seis categorias do modelo vocacional RIASEC (Holland, 1959), usado
 * como base cientifica da regra de negocio de pontuacao do teste vocacional.
 * Cada categoria e associada a uma lista de areas/cursos recomendados.
 */
public enum CategoriaRiasec {

    REALISTA("Realista", List.of("Engenharias", "Tecnologia da Informação", "Agronomia")),
    INVESTIGATIVO("Investigativo", List.of("Ciências Exatas", "Ciências Biológicas", "Medicina")),
    ARTISTICO("Artístico", List.of("Design", "Arquitetura e Urbanismo", "Comunicação Social")),
    SOCIAL("Social", List.of("Pedagogia", "Psicologia", "Enfermagem")),
    EMPREENDEDOR("Empreendedor", List.of("Administração", "Direito", "Economia")),
    CONVENCIONAL("Convencional", List.of("Ciências Contábeis", "Gestão Pública", "Biblioteconomia"));

    private final String nomeExibicao;
    private final List<String> areasRecomendadas;

    CategoriaRiasec(String nomeExibicao, List<String> areasRecomendadas) {
        this.nomeExibicao = nomeExibicao;
        this.areasRecomendadas = areasRecomendadas;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public List<String> getAreasRecomendadas() {
        return areasRecomendadas;
    }
}
