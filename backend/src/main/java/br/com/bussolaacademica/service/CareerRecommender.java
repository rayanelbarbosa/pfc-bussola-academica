package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import org.springframework.stereotype.Component;

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
 * </ol>
 *
 * <p>Complexidade: O(c log c), por causa da ordenação das c carreiras das áreas consideradas (a correlação é O(6) =
 * O(1) por carreira; a comparação entre áreas é mais uma passada, O(c)).</p>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 5;

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

    /** Ranking único das áreas consideradas e a comparação entre essas áreas. */
    public record Recommendation(List<RankedCareer> careers, List<AreaMatch> areaMatches) {
    }

    /**
     * As áreas escolhidas funcionam como filtro: as carreiras de todas elas entram num ranking único e o estudante
     * vê as {@value #LIMIT} melhores, de qualquer uma das áreas. "Ainda não sei" considera todas as áreas.
     * A comparação entre áreas usa a melhor carreira de cada uma, na mesma ordem do ranking.
     */
    public Recommendation recommend(List<InterestArea> areas, List<Career> careers, Map<RiasecCategory, Integer> scores) {
        // sem área (resultados antigos) ou "Ainda não sei": todas as áreas
        Set<InterestArea> considered = areas.isEmpty() || areas.contains(InterestArea.UNDECIDED)
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
        return new Recommendation(ranked.stream().limit(LIMIT).toList(), matches);
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
