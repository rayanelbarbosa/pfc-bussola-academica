package br.com.bussolaacademica.model;

/**
 * Grandes áreas de interesse escolhidas pelo estudante antes do teste (de 1 a 3).
 * "Ainda não sei" (UNDECIDED) não pode ser combinada com outras áreas.
 */
public enum InterestArea {
    HEALTH("Saúde"),
    EXACT_AND_TECHNOLOGY("Exatas e Tecnologia"),
    ENGINEERING("Engenharias"),
    HUMANITIES("Humanas e Sociais"),
    BUSINESS("Negócios e Gestão"),
    ARTS_AND_COMMUNICATION("Artes, Design e Comunicação"),
    EDUCATION("Educação"),
    UNDECIDED("Ainda não sei");

    private final String label;

    InterestArea(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
