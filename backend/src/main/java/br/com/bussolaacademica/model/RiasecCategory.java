package br.com.bussolaacademica.model;

/**
 * As seis categorias do modelo vocacional RIASEC (Holland, 1959), base da regra de
 * negócio de pontuação do teste vocacional. Cada categoria tem um rótulo em português,
 * exibido ao usuário. As carreiras recomendadas vêm da tabela career (código de 2 letras).
 */
public enum RiasecCategory {

    REALISTIC("Realista"),
    INVESTIGATIVE("Investigativo"),
    ARTISTIC("Artístico"),
    SOCIAL("Social"),
    ENTERPRISING("Empreendedor"),
    CONVENTIONAL("Convencional");

    private final String label;

    RiasecCategory(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
