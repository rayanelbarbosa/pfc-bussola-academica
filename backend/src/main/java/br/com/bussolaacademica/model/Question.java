package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Pergunta do questionário vocacional. Cada pergunta pertence a exatamente uma
 * categoria RIASEC. As perguntas ficam no banco (carga inicial via Flyway), o que
 * permite ajustá-las sem recompilar o sistema.
 */
@Entity
@Table(name = "question")
public class Question {

    @Id
    private Integer id;

    @Column(nullable = false, length = 300)
    private String statement;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 20)
    private RiasecCategory category;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected Question() {
        // exigido pelo JPA
    }

    public Question(Integer id, String statement, RiasecCategory category, int displayOrder) {
        this.id = id;
        this.statement = statement;
        this.category = category;
        this.displayOrder = displayOrder;
    }

    public Integer getId() {
        return id;
    }

    public String getStatement() {
        return statement;
    }

    public RiasecCategory getCategory() {
        return category;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
