package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.service.CareerRecommender.AreaMatch;
import br.com.bussolaacademica.service.CareerRecommender.Pick;
import br.com.bussolaacademica.service.CareerRecommender.RankedCareer;
import br.com.bussolaacademica.service.CareerRecommender.Recommendation;
import br.com.bussolaacademica.service.CareerRecommender.TieReason;
import org.assertj.core.data.Offset;
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
 * Regra de recomendação: compatibilidade = 2 × tipo principal + tipo secundário; desempate pelo perfil completo
 * do O*NET, depois pelo 3º tipo e pelo nome; ranking único das áreas escolhidas, com as 4 melhores.
 */
class CareerRecommenderTest {

    private final CareerRecommender recommender = new CareerRecommender();

    /** R=9, I=4, A=7, S=6, E=3, C=9 (exemplo de perfil com empate entre Realista e Convencional). */
    private static Map<RiasecCategory, Integer> scores() {
        return scores(9, 4, 7, 6, 3, 9);
    }

    private static Map<RiasecCategory, Integer> scores(int r, int i, int a, int s, int e, int c) {
        Map<RiasecCategory, Integer> scores = new EnumMap<>(RiasecCategory.class);
        scores.put(REALISTIC, r);
        scores.put(INVESTIGATIVE, i);
        scores.put(ARTISTIC, a);
        scores.put(SOCIAL, s);
        scores.put(ENTERPRISING, e);
        scores.put(CONVENTIONAL, c);
        return scores;
    }

    private static Career career(int id, String name, InterestArea area, RiasecCategory first, RiasecCategory second) {
        return new Career(id, name, area, first, second, "descrição");
    }

    private static List<String> names(List<RankedCareer> ranked) {
        return ranked.stream().map(item -> item.career().getName()).toList();
    }

    @Test
    void compatibilityWeightsDominantTypeTwice() {
        Career civil = career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE);
        assertThat(CareerRecommender.compatibility(civil, scores())).isEqualTo(2 * 9 + 4);
        assertThat(civil.getCode()).isEqualTo("RI");
    }

    @Test
    void chosenAreasAreAFilterForASingleRankingOfFour() {
        List<Career> careers = List.of(
                career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),        // 22
                career(2, "Engenharia de Produção", InterestArea.ENGINEERING, ENTERPRISING, CONVENTIONAL), // 15
                career(3, "Engenharia Química", InterestArea.ENGINEERING, INVESTIGATIVE, REALISTIC),      // 17
                career(4, "Contabilidade", InterestArea.BUSINESS, CONVENTIONAL, REALISTIC),               // 27
                career(5, "Marketing", InterestArea.BUSINESS, ENTERPRISING, ARTISTIC),                    // 13
                career(6, "Logística", InterestArea.BUSINESS, CONVENTIONAL, ENTERPRISING),                // 21
                career(7, "Design Gráfico", InterestArea.ARTS_AND_COMMUNICATION, ARTISTIC, REALISTIC));   // 23, área não escolhida

        Recommendation result = recommender.recommend(List.of(InterestArea.ENGINEERING, InterestArea.BUSINESS),
                careers, scores());

        assertThat(names(result.careers()))
                .containsExactly("Contabilidade", "Engenharia Civil", "Logística", "Engenharia Química");
        assertThat(result.careers()).extracting(RankedCareer::compatibility).containsExactly(27, 22, 21, 17);
        // sem empate, nenhuma explicação de desempate
        assertThat(result.picks()).allSatisfy(pick -> assertThat(pick.reason()).isNull());
        // a área que mais combina vem primeiro, com a melhor carreira dela
        assertThat(result.areaMatches()).extracting(AreaMatch::area)
                .containsExactly(InterestArea.BUSINESS, InterestArea.ENGINEERING);
        assertThat(result.areaMatches().get(1).best().career().getName()).isEqualTo("Engenharia Civil");
        // Design Gráfico (23) não supera Contabilidade (27): sem aviso
        assertThat(result.suggestions()).isEmpty();
    }

    @Test
    void suggestsTheCareersThatFitBestWhenTheChosenAreasFitPoorly() {
        // perfil Artístico (A=10) que escolheu só Engenharias
        Map<RiasecCategory, Integer> artistic = scores(4, 4, 10, 6, 5, 4);
        List<Career> careers = List.of(
                career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),        // 12
                career(2, "Engenharia Mecânica", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),     // 12
                career(3, "Artes Cênicas", InterestArea.ARTS_AND_COMMUNICATION, ARTISTIC, SOCIAL),        // 26
                career(4, "Design Gráfico", InterestArea.ARTS_AND_COMMUNICATION, ARTISTIC, CONVENTIONAL), // 24
                career(5, "Pedagogia", InterestArea.EDUCATION, SOCIAL, ARTISTIC));                       // 22

        Recommendation result = recommender.recommend(List.of(InterestArea.ENGINEERING), careers, artistic);

        // a escolha do estudante é respeitada: o ranking principal continua sendo das Engenharias
        assertThat(names(result.careers())).containsExactly("Engenharia Civil", "Engenharia Mecânica");
        // e ele recebe também as que mais combinam com ele, de todas as áreas
        assertThat(result.suggestions()).extracting(pick -> pick.ranked().career().getName())
                .containsExactly("Artes Cênicas", "Design Gráfico", "Pedagogia", "Engenharia Civil");
        assertThat(result.suggestions().get(0).ranked().compatibility()).isEqualTo(26);
    }

    @Test
    void noSuggestionWhenTheDifferenceIsSmallOrTheStudentIsUndecided() {
        Map<RiasecCategory, Integer> balanced = scores(8, 7, 9, 5, 5, 5);
        List<Career> careers = List.of(
                career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),   // 23
                career(2, "Design de Moda", InterestArea.ARTS_AND_COMMUNICATION, ARTISTIC, REALISTIC)); // 26: só 3 a mais

        assertThat(recommender.recommend(List.of(InterestArea.ENGINEERING), careers, balanced).suggestions()).isEmpty();
        assertThat(recommender.recommend(List.of(InterestArea.UNDECIDED), careers, balanced).suggestions()).isEmpty();
    }

    @Test
    void undecidedConsidersEveryArea() {
        List<Career> careers = List.of(
                career(1, "Engenharia Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE),
                career(2, "Contabilidade", InterestArea.BUSINESS, CONVENTIONAL, REALISTIC),
                career(3, "Pedagogia", InterestArea.EDUCATION, SOCIAL, ARTISTIC),
                career(4, "Marketing", InterestArea.BUSINESS, ENTERPRISING, ARTISTIC));

        Recommendation result = recommender.recommend(List.of(InterestArea.UNDECIDED), careers, scores());

        assertThat(names(result.careers())).containsExactly("Contabilidade", "Engenharia Civil", "Pedagogia", "Marketing");
        assertThat(result.areaMatches()).extracting(AreaMatch::area)
                .containsExactly(InterestArea.BUSINESS, InterestArea.ENGINEERING, InterestArea.EDUCATION);
        // resultados antigos sem área também consideram todas
        assertThat(recommender.recommend(List.of(), careers, scores()).careers()).hasSize(4);
    }

    @Test
    void breaksTiesWithTheFullOnetProfileBeforeTheThirdType() {
        // Mesmo código RIC: compatibilidade e 3º tipo iguais; só o perfil completo do O*NET diferencia
        Career mecanica = new Career(1, "Mecânica", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL, "d");
        mecanica.setInterestProfile("17-2141.00", 6.39, 5.12, 2.02, 1.22, 1.97, 4.83);
        Career civil = new Career(2, "Civil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL, "d");
        civil.setInterestProfile("17-2051.00", 6.43, 5.15, 2.21, 1.69, 2.66, 4.61);
        Career semPerfil = new Career(3, "Aaa sem perfil", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL, "d");

        // aluno com Social e Empreendedor baixos: o perfil da Mecânica (S = 1,22) é o mais parecido
        List<RankedCareer> top = recommender.recommend(List.of(InterestArea.ENGINEERING),
                List.of(semPerfil, civil, mecanica), scores(10, 8, 2, 2, 2, 8)).careers();

        assertThat(names(top)).containsExactly("Mecânica", "Civil", "Aaa sem perfil");
        assertThat(top.get(0).similarity()).isGreaterThan(top.get(1).similarity());
        assertThat(top.get(2).similarity()).isEqualTo(CareerRecommender.NO_PROFILE);

        // cada carreira explica por que ficou na frente da seguinte, que empatou com ela
        List<Pick> picks = recommender.recommend(List.of(InterestArea.ENGINEERING),
                List.of(semPerfil, civil, mecanica), scores(10, 8, 2, 2, 2, 8)).picks();
        assertThat(picks.get(0).aheadOf().career().getName()).isEqualTo("Civil");
        assertThat(picks.get(0).reason()).isEqualTo(TieReason.PROFILE);
        assertThat(picks.get(1).reason()).isEqualTo(TieReason.PROFILE);
        assertThat(picks.get(2).aheadOf()).isNull();
    }

    @Test
    void withoutProfileTheThirdTypeBreaksTheTieAndThenTheName() {
        // todas empatam em 2 × 9 + 4 = 22 (código RI); o 3º tipo decide: C=9 > A=7 > S=6 > sem 3º tipo
        List<Career> careers = List.of(
                new Career(1, "Sem terceiro", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, "d"),
                new Career(2, "Social", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, SOCIAL, "d"),
                new Career(3, "Artistico", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, ARTISTIC, "d"),
                new Career(4, "Convencional", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL, "d"),
                new Career(5, "Biologia", InterestArea.ENGINEERING, REALISTIC, INVESTIGATIVE, CONVENTIONAL, "d"));

        List<Pick> picks = recommender.recommend(List.of(InterestArea.ENGINEERING), careers, scores()).picks();

        assertThat(picks).extracting(pick -> pick.ranked().career().getName())
                .containsExactly("Biologia", "Convencional", "Artistico", "Social");
        assertThat(picks.get(0).ranked().career().getCode()).isEqualTo("RIC");
        // mesmo código completo: só o nome separa; depois, o 3º tipo
        assertThat(picks).extracting(Pick::reason)
                .containsExactly(TieReason.NAME, TieReason.THIRD_TYPE, TieReason.THIRD_TYPE, TieReason.THIRD_TYPE);
        // a 4ª explica por que ficou na frente da que ficou de fora
        assertThat(picks.get(3).aheadOf().career().getName()).isEqualTo("Sem terceiro");
    }

    @Test
    void showsAtMostFour() {
        List<Career> careers = java.util.stream.IntStream.rangeClosed(1, 8)
                .mapToObj(i -> career(i, "Carreira " + i, InterestArea.HEALTH, SOCIAL, INVESTIGATIVE))
                .toList();

        assertThat(recommender.recommend(List.of(InterestArea.HEALTH), careers, scores()).careers()).hasSize(4);
    }

    @Test
    void pearsonCorrelationIgnoresScaleAndHandlesFlatProfiles() {
        assertThat(CareerRecommender.pearson(new double[] {2, 4, 6, 8, 10, 10}, new double[] {1, 2, 3, 4, 5, 5}))
                .isCloseTo(1.0, Offset.offset(1e-9));
        assertThat(CareerRecommender.pearson(new double[] {2, 4, 6, 8, 10, 10}, new double[] {5, 4, 3, 2, 1, 1}))
                .isCloseTo(-1.0, Offset.offset(1e-9));
        assertThat(CareerRecommender.pearson(new double[] {6, 6, 6, 6, 6, 6}, new double[] {1, 2, 3, 4, 5, 6})).isZero();
    }
}
