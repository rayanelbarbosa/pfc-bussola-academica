package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.service.CareerRecommender.Recommendation;
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

/**
 * Regra de recomendação (concordância da primeira letra): pontos no 1º tipo da carreira, depois no 2º, no 3º e o nome;
 * as 4 primeiras das áreas escolhidas; aviso quando nenhuma delas tem o perfil como tipo principal; ranking completo.
 * Exemplo da Ana: R=4, I=10, A=14, S=17, E=8, C=5 (perfil Social), com os códigos reais do banco.
 */
class CareerRecommenderTest {

    private final CareerRecommender recommender = new CareerRecommender();

    private static final Career PEDAGOGIA = career(1, "Pedagogia", InterestArea.EDUCATION, SOCIAL, ARTISTIC, CONVENTIONAL);
    private static final Career PSICOLOGIA = career(2, "Psicologia", InterestArea.HUMANITIES, SOCIAL, INVESTIGATIVE, CONVENTIONAL);
    private static final Career FISIOTERAPIA = career(3, "Fisioterapia", InterestArea.HEALTH, SOCIAL, INVESTIGATIVE, REALISTIC);
    private static final Career ENFERMAGEM = career(4, "Enfermagem", InterestArea.HEALTH, SOCIAL, CONVENTIONAL, INVESTIGATIVE);
    private static final Career MEDICINA = career(5, "Medicina", InterestArea.HEALTH, INVESTIGATIVE, SOCIAL, REALISTIC);
    private static final Career CIVIL = career(6, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL);
    private static final Career MECANICA = career(7, "Engenharia Mecânica", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE,
            CONVENTIONAL);

    private static final List<Career> ALL = List.of(CIVIL, MEDICINA, ENFERMAGEM, PSICOLOGIA, MECANICA, PEDAGOGIA,
            FISIOTERAPIA);

    private static Map<RiasecCategory, Integer> ana() {
        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        scores.put(REALISTIC, 4);
        scores.put(INVESTIGATIVE, 10);
        scores.put(ARTISTIC, 14);
        scores.put(SOCIAL, 17);
        scores.put(ENTERPRISING, 8);
        scores.put(CONVENTIONAL, 5);
        return scores;
    }

    private static Career career(int id, String name, InterestArea area, RiasecCategory first, RiasecCategory second,
                                 RiasecCategory third) {
        return new Career(id, name, area, first, second, third, "descrição");
    }

    private static List<String> names(List<Career> careers) {
        return careers.stream().map(Career::getName).toList();
    }

    @Test
    void ranksByThePointsInTheFirstSecondAndThirdTypesThenTheName() {
        List<Career> ranking = recommender.rank(ALL, ana());

        // Social (17) primeiro; entre as sociais, o 2º tipo: Artístico 14 > Investigativo 10 > Convencional 5;
        // Psicologia (SIC) e Fisioterapia (SIR) empatam nos 2 primeiros e o 3º tipo decide: Convencional 5 > Realista 4;
        // Civil e Mecânica têm o mesmo código (RIC) e ficam em ordem alfabética
        assertThat(names(ranking)).containsExactly("Pedagogia", "Psicologia", "Fisioterapia", "Enfermagem",
                "Medicina", "Engenharia Civil", "Engenharia Mecânica");
    }

    @Test
    void careerWithoutThirdTypeCountsZeroInTheThirdType() {
        Career semTerceiro = new Career(8, "Aaa sem 3º tipo", InterestArea.HUMANITIES, SOCIAL, INVESTIGATIVE, "d");

        assertThat(CareerRecommender.thirdTypeScore(semTerceiro, ana())).isZero();
        assertThat(names(recommender.rank(List.of(semTerceiro, FISIOTERAPIA), ana())))
                .containsExactly("Fisioterapia", "Aaa sem 3º tipo");
    }

    @Test
    void alwaysReturnsTheFullRankingOfAllAreas() {
        Recommendation recommendation = recommender.recommend(List.of(InterestArea.HEALTH), ALL, ana(), List.of(SOCIAL));

        assertThat(recommendation.ranking()).hasSize(ALL.size());
        assertThat(recommendation.ranking().get(6).getName()).isEqualTo("Engenharia Mecânica");
    }

    @Test
    void undecidedRecommendsTheFirstFourOfAllAreasWithoutWarning() {
        Recommendation recommendation = recommender.recommend(List.of(InterestArea.UNDECIDED), ALL, ana(), List.of(SOCIAL));

        assertThat(names(recommendation.careers())).containsExactly("Pedagogia", "Psicologia", "Fisioterapia", "Enfermagem");
        assertThat(recommendation.suggestions()).isEmpty();
    }

    @Test
    void oldResultWithoutAreasIsTreatedAsUndecided() {
        Recommendation recommendation = recommender.recommend(List.of(), ALL, ana(), List.of(SOCIAL));

        assertThat(recommendation.careers()).hasSize(4);
        assertThat(recommendation.suggestions()).isEmpty();
    }

    @Test
    void chosenAreasFilterTheRankingAndNoWarningWhenOneCareerHasTheProfileAsMainType() {
        Recommendation recommendation = recommender.recommend(List.of(InterestArea.HEALTH), ALL, ana(), List.of(SOCIAL));

        assertThat(names(recommendation.careers())).containsExactly("Fisioterapia", "Enfermagem", "Medicina");
        assertThat(recommendation.suggestions()).isEmpty();
    }

    @Test
    void warnsWhenNoCareerOfTheChosenAreasHasTheProfileAsMainType() {
        Recommendation recommendation = recommender.recommend(List.of(InterestArea.ENGINEERING), ALL, ana(), List.of(SOCIAL));

        // a escolha é respeitada: as engenharias continuam na lista principal
        assertThat(names(recommendation.careers())).containsExactly("Engenharia Civil", "Engenharia Mecânica");
        // e o aviso traz as 4 que mais combinam com ela em todas as áreas
        assertThat(names(recommendation.suggestions())).containsExactly("Pedagogia", "Psicologia", "Fisioterapia",
                "Enfermagem");
    }

    @Test
    void tiedProfileCountsAnyOfItsTypesForTheWarning() {
        Map<RiasecCategory, Integer> tied = ana();
        tied.put(REALISTIC, 17);

        Recommendation recommendation = recommender.recommend(List.of(InterestArea.ENGINEERING), ALL, tied,
                List.of(REALISTIC, SOCIAL));

        assertThat(recommendation.suggestions()).isEmpty();
    }
}
