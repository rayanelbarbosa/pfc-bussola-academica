package br.com.bussolaacademica.model;

/** Tipos de forma de ingresso no ensino superior, com o rótulo exibido ao usuário. */
public enum AdmissionType {
    NATIONAL_EXAM("Exame nacional"),
    GOVERNMENT_PROGRAM("Programa do governo"),
    STUDENT_FINANCING("Financiamento estudantil"),
    ENTRANCE_EXAM("Vestibular"),
    SCHOLARSHIP_PORTAL("Portal de bolsas");

    private final String label;

    AdmissionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
