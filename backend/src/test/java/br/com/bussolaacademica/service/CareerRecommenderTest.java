package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.service.CareerRecommender.RankedCareer;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static br.com.bussolaacademica.model.RiasecCategory.ARTISTIC;
import static br.com.bussolaacademica.model.RiasecCategory.CONVENTIONAL;
import static br.com.bussolaacademica.model.RiasecCategory.ENTERPRISING;
import static br.com.bussolaacademica.model.RiasecCategory.INVESTIGATIVE;
import static br.com.bussolaacademica.model.RiasecCategory.REALISTIC;
import static br.com.bussolaacademica.model.RiasecCategory.SOCIAL;
import static org.assertj.core.api.Assertions.assertThat;

/** Regra de recomendação de carreiras: compatibilidade = 2 × tipo dominante + tipo secundário. */
class CareerRecommenderTest {

    private final CareerRecommender recommender = new CareerRecommender();

    /** R=9, I=4, A=7, S=6, E=3, C=9 (exemplo de perfil com empate entre Realista e Convencional). */
    private static Map<RiasecCategory, Integer> scores() {
        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        scores.put(REALISTIC, 9);
        scores.put(INVESTIGATIVE, 4);
        scores.put(ARTISTIC, 7);
        scores.put(SOCIAL, 6);
        scores.put(ENTERPRISING, 3);
        scores.put(CONVENTIONAL, 9);
        return scores;
    }

    private static Career career(int id, String name, InterestArea area, RiasecCategory first, RiasecCategory second) {
        return new Career(id, name, area, first, second, "descrição");
    }

    @Test
    void compatibilityWeightsDominantTypeTwice() {
        Career civil = career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE);
        assertThat(CareerRecommender.compatibility(civil, scores())).isEqualTo(2 * 9 + 4);
        assertThat(civil.getCode()).isEqualTo("RI");
    }

    @Test
    void recommendsTopThreePerChosenAreaIgnoringOtherAreas() {
        List<Career> careers = List.of(
                career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),       // 22
                career(2, "Engenharia de Produção", InterestArea.ENGINEERING, ENTERPRISING, CONVENTIONAL), // 15
                career(3, "Engenharia Química", InterestArea.ENGINEERING, INVESTIGATIVE, REALISTIC),     // 17
                career(4, "Engenharia Ambiental", InterestArea.ENGINEERING, INVESTIGATIVE, SOCIAL),      // 14
                career(5, "Design Gráfico", InterestArea.ARTS_AND_COMMUNICATION, ARTISTIC, REALISTIC));  // 23

        Map<InterestArea, List<RankedCareer>> result =
                recommender.recommend(List.of(InterestArea.ENGINEERING), careers, scores());

        assertThat(result).containsOnlyKeys(InterestArea.ENGINEERING);
        assertThat(result.get(InterestArea.ENGINEERING))
                .extracting(ranked -> ranked.career().getName(), RankedCareer::compatibility)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("Engenharia Civil", 22),
                        org.assertj.core.groups.Tuple.tuple("Engenharia Química", 17),
                        org.assertj.core.groups.Tuple.tuple("Engenharia de Produção", 15));
    }

    @Test
    void breaksTiesWithTheThirdTypeAndShowsExactlyThree() {
        // A, B, C e D empatam em 2 × 9 + 4 = 22 (código RI); o 3º tipo decide: C=9 > A=7 > S=6 > sem 3º tipo
        List<Career> careers = List.of(
                new Career(1, "Sem terceiro", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, "d"),
                new Career(2, "Social", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, SOCIAL, "d"),
                new Career(3, "Artistico", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, ARTISTIC, "d"),
                new Career(4, "Convencional", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL, "d"),
                new Career(5, "Longe", InterestArea.ENGINEERING, ENTERPRISING, INVESTIGATIVE, "d"));

        List<RankedCareer> top = recommender.recommend(List.of(InterestArea.ENGINEERING), careers, scores())
                .get(InterestArea.ENGINEERING);

        assertThat(top).extracting(ranked -> ranked.career().getName())
                .containsExactly("Convencional", "Artistico", "Social");
        assertThat(top).extracting(RankedCareer::compatibility).containsOnly(22);
        assertThat(top.get(0).career().getCode()).isEqualTo("RIC");
    }

    @Test
    void sameFullCodeFallsBackToAlphabeticalOrder() {
        List<Career> careers = List.of(
                new Career(1, "Zootecnia", InterestArea.HEALTH, SOCIAL, REALISTIC, ARTISTIC, "d"),
                new Career(2, "Biologia", InterestArea.HEALTH, SOCIAL, REALISTIC, ARTISTIC, "d"));

        assertThat(recommender.recommend(List.of(InterestArea.HEALTH), careers, scores()).get(InterestArea.HEALTH))
                .extracting(ranked -> ranked.career().getName())
                .containsExactly("Biologia", "Zootecnia");
    }

    /** "Ainda não sei" mostra até 5 carreiras de todas as áreas (aqui só existem 4). */
    @Test
    void undecidedRanksCareersFromAllAreasAndKeepsChosenOrder() {
        List<Career> careers = List.of(
                career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),
                career(2, "Contabilidade", InterestArea.BUSINESS, CONVENTIONAL, REALISTIC),
                career(3, "Pedagogia", InterestArea.EDUCATION, SOCIAL, ARTISTIC),
                career(4, "Marketing", InterestArea.BUSINESS, ENTERPRISING, ARTISTIC));

        assertThat(recommender.recommend(List.of(InterestArea.UNDECIDED), careers, scores())
                .get(InterestArea.UNDECIDED))
                .extracting(ranked -> ranked.career().getName())
                .containsExactly("Contabilidade", "Engenharia Civil", "Pedagogia", "Marketing");

        assertThat(recommender.recommend(List.of(InterestArea.EDUCATION, InterestArea.BUSINESS), careers, scores()))
                .containsOnlyKeys(InterestArea.EDUCATION, InterestArea.BUSINESS)
                .extractingByKey(InterestArea.BUSINESS)
                .satisfies(list -> assertThat(list).hasSize(2));
    }
}
