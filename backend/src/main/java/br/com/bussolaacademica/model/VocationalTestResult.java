package br.com.bussolaacademica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado persistido de uma aplicação do teste vocacional: o escore de cada
 * categoria RIASEC, a(s) categoria(s) predominante(s), as áreas recomendadas e as áreas de interesse escolhidas.
 */
@Entity
@Table(name = "vocational_test_result")
public class VocationalTestResult {

    private static final String SEPARATOR = ";";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Dono do resultado: só ele (ou um administrador) pode consultá-lo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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

    /** Áreas de interesse escolhidas antes do teste, ex.: "HEALTH;ENGINEERING". Nulo = "Ainda não sei". */
    @Column(name = "interest_areas", length = 200)
    private String interestAreas;

    protected VocationalTestResult() {
        // exigido pelo JPA
    }

    public VocationalTestResult(User user, LocalDateTime createdAt, Map<RiasecCategory, Integer> scores,
                                List<RiasecCategory> topCategories, List<InterestArea> interestAreas) {
        this.user = user;
        this.createdAt = createdAt;
        this.realisticScore = scores.get(RiasecCategory.REALISTIC);
        this.investigativeScore = scores.get(RiasecCategory.INVESTIGATIVE);
        this.artisticScore = scores.get(RiasecCategory.ARTISTIC);
        this.socialScore = scores.get(RiasecCategory.SOCIAL);
        this.enterprisingScore = scores.get(RiasecCategory.ENTERPRISING);
        this.conventionalScore = scores.get(RiasecCategory.CONVENTIONAL);
        this.topCategories = String.join(SEPARATOR, topCategories.stream().map(Enum::name).toList());
        this.interestAreas = String.join(SEPARATOR, interestAreas.stream().map(Enum::name).toList());
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

    public List<InterestArea> getInterestAreas() {
        if (interestAreas == null || interestAreas.isBlank()) {
            return List.of(InterestArea.UNDECIDED);
        }
        return Arrays.stream(interestAreas.split(SEPARATOR)).map(InterestArea::valueOf).toList();
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    /** true se o resultado pertence ao usuário informado. */
    public boolean belongsTo(Long userId) {
        return user != null && user.getId().equals(userId);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
