package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado persistido de uma aplicação do teste vocacional: o escore de cada
 * categoria RIASEC, a(s) categoria(s) predominante(s) e as áreas recomendadas.
 */
@Entity
@Table(name = "vocational_test_result")
public class VocationalTestResult {

    private static final String SEPARATOR = ";";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "realistic_score", nullable = false)
    private int realisticScore;

    @Column(name = "investigative_score", nullable = false)
    private int investigativeScore;

    @Column(name = "artistic_score", nullable = false)
    private int artisticScore;

    @Column(name = "social_score", nullable = false)
    private int socialScore;

    @Column(name = "enterprising_score", nullable = false)
    private int enterprisingScore;

    @Column(name = "conventional_score", nullable = false)
    private int conventionalScore;

    /** Códigos das categorias predominantes, ex.: "INVESTIGATIVE;SOCIAL". */
    @Column(name = "top_categories", nullable = false, length = 100)
    private String topCategories;

    @Column(name = "recommended_areas", nullable = false, length = 500)
    private String recommendedAreas;

    protected VocationalTestResult() {
        // exigido pelo JPA
    }

    public VocationalTestResult(LocalDateTime createdAt, Map<RiasecCategory, Integer> scores,
                                List<RiasecCategory> topCategories, List<String> recommendedAreas) {
        this.createdAt = createdAt;
        this.realisticScore = scores.get(RiasecCategory.REALISTIC);
        this.investigativeScore = scores.get(RiasecCategory.INVESTIGATIVE);
        this.artisticScore = scores.get(RiasecCategory.ARTISTIC);
        this.socialScore = scores.get(RiasecCategory.SOCIAL);
        this.enterprisingScore = scores.get(RiasecCategory.ENTERPRISING);
        this.conventionalScore = scores.get(RiasecCategory.CONVENTIONAL);
        this.topCategories = String.join(SEPARATOR, topCategories.stream().map(Enum::name).toList());
        this.recommendedAreas = String.join(SEPARATOR, recommendedAreas);
    }

    /** Escores na ordem oficial do RIASEC (R, I, A, S, E, C). */
    public Map<RiasecCategory, Integer> getScores() {
        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        scores.put(RiasecCategory.REALISTIC, realisticScore);
        scores.put(RiasecCategory.INVESTIGATIVE, investigativeScore);
        scores.put(RiasecCategory.ARTISTIC, artisticScore);
        scores.put(RiasecCategory.SOCIAL, socialScore);
        scores.put(RiasecCategory.ENTERPRISING, enterprisingScore);
        scores.put(RiasecCategory.CONVENTIONAL, conventionalScore);
        return scores;
    }

    public List<RiasecCategory> getTopCategories() {
        return Arrays.stream(topCategories.split(SEPARATOR)).map(RiasecCategory::valueOf).toList();
    }

    public List<String> getRecommendedAreas() {
        return Arrays.asList(recommendedAreas.split(SEPARATOR));
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
