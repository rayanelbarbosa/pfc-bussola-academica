package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regra de negócio: recomendação de carreiras por área de interesse.
 *
 * <ol>
 *   <li>Cada carreira tem um código de Holland: tipo principal, secundário e, opcionalmente, um 3º tipo.</li>
 *   <li>Compatibilidade = 2 × escore do tipo principal + escore do tipo secundário
 *       (o tipo principal pesa o dobro; vai de 6 a 30 pontos).</li>
 *   <li>Desempate: semelhança entre o perfil completo do estudante (6 escores) e o da profissão no O*NET
 *       (6 notas), medida pela correlação de Pearson. Carreira sem perfil O*NET fica atrás e desempata pelo
 *       escore do estudante no 3º tipo do código; persistindo, ordem alfabética.</li>
 *   <li>As áreas escolhidas (até 3) são um filtro: as carreiras delas entram num ranking único e o estudante vê
 *       as {@value #LIMIT} melhores, junto com a comparação entre as áreas (qual combina mais com ele). Poucas
 *       opções, bem ordenadas, ajudam mais quem está em dúvida do que uma lista longa.</li>
 *   <li>"Ainda não sei": o ranking considera as carreiras de todas as áreas.</li>
 *   <li>Aviso de área que combina pouco: quando a melhor carreira de fora das áreas escolhidas tem pelo menos
 *       {@value #SUGGESTION_GAP} pontos a mais que a melhor das áreas escolhidas, o estudante recebe também as
 *       {@value #LIMIT} carreiras que mais combinam com ele em todas as áreas. A escolha dele é respeitada (ele
 *       continua vendo as áreas que escolheu) e decide qual lista quer ver.</li>
 * </ol>
 *
 * <p>Complexidade: O(c log c), por causa da ordenação das c carreiras das áreas consideradas (a correlação é O(6) =
 * O(1) por carreira; a comparação entre áreas é mais uma passada, O(c)).</p>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 4;

    /**
     * Diferença mínima de compatibilidade para sugerir outras áreas: 4 pontos equivalem a 2 pontos a mais no tipo
     * principal (que pesa o dobro), ou seja, uma resposta inteira de diferença em cada uma das 2 perguntas dele.
     * Diferenças menores são pequenas demais para justificar o aviso.
     */
    static final int SUGGESTION_GAP = 4;

    /** Valor de semelhança das carreiras sem perfil O*NET: abaixo de qualquer correlação possível (-1 a 1). */
    static final double NO_PROFILE = -2.0;

    public record RankedCareer(Career career, int compatibility, double similarity, int tieBreaker) {
    }

    public static int compatibility(Career career, Map<RiasecCategory, Integer> scores) {
        return 2 * scores.get(career.getPrimaryCategory()) + scores.get(career.getSecondaryCategory());
    }

    /**
     * Correlação de Pearson entre os 6 escores do estudante (R, I, A, S, E, C) e as 6 notas da profissão no O*NET:
     * 1 = perfis com o mesmo formato, 0 = sem relação, -1 = opostos. Não depende da escala (2 a 10 contra 1 a 7).
     * Perfil do estudante todo igual (sem variação) dá 0.
     */
    public static double similarity(Career career, Map<RiasecCategory, Integer> scores) {
        double[] profile = career.getInterestProfile();
        if (profile == null) {
            return NO_PROFILE;
        }
        RiasecCategory[] order = RiasecCategory.values();
        double[] student = new double[order.length];
        for (int i = 0; i < order.length; i++) {
            student[i] = scores.get(order[i]);
        }
        return pearson(student, profile);
    }

    static double pearson(double[] x, double[] y) {
        double meanX = 0;
        double meanY = 0;
        for (int i = 0; i < x.length; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        meanX /= x.length;
        meanY /= y.length;
        double covariance = 0;
        double varianceX = 0;
        double varianceY = 0;
        for (int i = 0; i < x.length; i++) {
            covariance += (x[i] - meanX) * (y[i] - meanY);
            varianceX += (x[i] - meanX) * (x[i] - meanX);
            varianceY += (y[i] - meanY) * (y[i] - meanY);
        }
        if (varianceX == 0 || varianceY == 0) {
            return 0;
        }
        return covariance / Math.sqrt(varianceX * varianceY);
    }

    /** Escore do estudante no 3º tipo da carreira (0 quando a carreira não tem 3º tipo). */
    public static int tieBreaker(Career career, Map<RiasecCategory, Integer> scores) {
        return career.getTertiaryCategory() == null ? 0 : scores.get(career.getTertiaryCategory());
    }

    /** Melhor carreira de uma área: mostra ao estudante qual das áreas combina mais com o perfil dele. */
    public record AreaMatch(InterestArea area, RankedCareer best) {
    }

    /** Critério que separou duas carreiras empatadas na compatibilidade. */
    public enum TieReason { PROFILE, THIRD_TYPE, NAME }

    /**
     * Carreira do ranking com a explicação da posição: quando empata na compatibilidade com a seguinte (mesmo a
     * que ficou fora das {@value #LIMIT}), diz qual foi e o critério que deixou esta na frente. Sem empate, os
     * dois campos ficam nulos.
     */
    public record Pick(RankedCareer ranked, RankedCareer aheadOf, TieReason reason) {
    }

    /**
     * Ranking único das áreas consideradas, a comparação entre essas áreas e, quando as áreas escolhidas combinam
     * pouco com o perfil, as carreiras que mais combinam com ele em todas as áreas (lista vazia quando não há aviso).
     */
    public record Recommendation(List<Pick> picks, List<AreaMatch> areaMatches, List<Pick> suggestions) {

        public List<RankedCareer> careers() {
            return picks.stream().map(Pick::ranked).toList();
        }
    }

    /**
     * As áreas escolhidas funcionam como filtro: as carreiras de todas elas entram num ranking único e o estudante
     * vê as {@value #LIMIT} melhores, de qualquer uma das áreas. "Ainda não sei" considera todas as áreas.
     * A comparação entre áreas usa a melhor carreira de cada uma, na mesma ordem do ranking.
     */
    public Recommendation recommend(List<InterestArea> areas, List<Career> careers, Map<RiasecCategory, Integer> scores) {
        // sem área (resultados antigos) ou "Ainda não sei": todas as áreas
        boolean allAreas = areas.isEmpty() || areas.contains(InterestArea.UNDECIDED);
        Set<InterestArea> considered = allAreas
                ? EnumSet.complementOf(EnumSet.of(InterestArea.UNDECIDED))
                : EnumSet.copyOf(areas);
        List<RankedCareer> ranked = rank(careers.stream().filter(career -> considered.contains(career.getArea())).toList(),
                scores);

        // a 1ª vez que cada área aparece no ranking ordenado é a melhor carreira dela: uma passada, O(c)
        Map<InterestArea, RankedCareer> bestByArea = new LinkedHashMap<>();
        for (RankedCareer item : ranked) {
            bestByArea.putIfAbsent(item.career().getArea(), item);
        }
        List<AreaMatch> matches = bestByArea.entrySet().stream()
                .map(entry -> new AreaMatch(entry.getKey(), entry.getValue()))
                .toList();
        List<Pick> suggestions = allAreas ? List.of() : suggestions(ranked, careers, scores);
        return new Recommendation(top(ranked), matches, suggestions);
    }

    /**
     * Carreiras de todas as áreas, quando a melhor delas supera a melhor das áreas escolhidas em pelo menos
     * {@value #SUGGESTION_GAP} pontos; senão, lista vazia. Mais um ranking: O(c log c).
     */
    List<Pick> suggestions(List<RankedCareer> chosen, List<Career> careers, Map<RiasecCategory, Integer> scores) {
        List<RankedCareer> everyArea = rank(careers.stream()
                .filter(career -> career.getArea() != InterestArea.UNDECIDED).toList(), scores);
        if (everyArea.isEmpty()) {
            return List.of();
        }
        int bestChosen = chosen.isEmpty() ? 0 : chosen.get(0).compatibility();
        return everyArea.get(0).compatibility() - bestChosen >= SUGGESTION_GAP ? top(everyArea) : List.of();
    }

    /** As {@value #LIMIT} primeiras do ranking, cada uma com o critério que a separou da seguinte, se empataram. */
    private static List<Pick> top(List<RankedCareer> ranked) {
        List<Pick> picks = new ArrayList<>();
        for (int i = 0; i < Math.min(LIMIT, ranked.size()); i++) {
            RankedCareer current = ranked.get(i);
            RankedCareer next = i + 1 < ranked.size() ? ranked.get(i + 1) : null;
            picks.add(next != null && next.compatibility() == current.compatibility()
                    ? new Pick(current, next, tieReason(current, next))
                    : new Pick(current, null, null));
        }
        return List.copyOf(picks);
    }

    /** Mesmo critério da ordenação: o primeiro que diferencia as duas carreiras empatadas. */
    static TieReason tieReason(RankedCareer ahead, RankedCareer behind) {
        if (Double.compare(ahead.similarity(), behind.similarity()) != 0) {
            return TieReason.PROFILE;
        }
        return ahead.tieBreaker() != behind.tieBreaker() ? TieReason.THIRD_TYPE : TieReason.NAME;
    }

    /** Ordena: compatibilidade, perfil completo do O*NET, 3º tipo e nome. */
    List<RankedCareer> rank(List<Career> candidates, Map<RiasecCategory, Integer> scores) {
        return candidates.stream()
                .map(career -> new RankedCareer(career, compatibility(career, scores), similarity(career, scores),
                        tieBreaker(career, scores)))
                .sorted(Comparator.comparingInt(RankedCareer::compatibility).reversed()
                        .thenComparing(Comparator.comparingDouble(RankedCareer::similarity).reversed())
                        .thenComparing(Comparator.comparingInt(RankedCareer::tieBreaker).reversed())
                        .thenComparing(ranked -> ranked.career().getName()))
                .toList();
    }
}
