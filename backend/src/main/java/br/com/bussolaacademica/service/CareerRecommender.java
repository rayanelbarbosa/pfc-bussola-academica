package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
 *   <li>Para cada área escolhida, recomenda exatamente as {@value #LIMIT} primeiras: poucas opções, bem
 *       ordenadas, ajudam mais quem está em dúvida do que uma lista longa de empatadas.</li>
 *   <li>"Ainda não sei": o ranking considera as carreiras de todas as áreas e mostra {@value #UNDECIDED_LIMIT}.</li>
 * </ol>
 *
 * <p>Complexidade: O(c log c) por área, por causa da ordenação das c carreiras candidatas (a correlação é O(6) = O(1)
 * por carreira).</p>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 3;
    static final int UNDECIDED_LIMIT = 5;

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

    /** Recomendações agrupadas por área, na ordem das áreas escolhidas. */
    public Map<InterestArea, List<RankedCareer>> recommend(List<InterestArea> areas, List<Career> careers,
                                                          Map<RiasecCategory, Integer> scores) {
        Map<InterestArea, List<RankedCareer>> result = new LinkedHashMap<>();
        for (InterestArea area : areas) {
            List<Career> candidates = area == InterestArea.UNDECIDED
                    ? careers
                    : careers.stream().filter(career -> career.getArea() == area).toList();
            result.put(area, top(candidates, scores, area == InterestArea.UNDECIDED ? UNDECIDED_LIMIT : LIMIT));
        }
        return result;
    }

    List<RankedCareer> top(List<Career> candidates, Map<RiasecCategory, Integer> scores, int limit) {
        return candidates.stream()
                .map(career -> new RankedCareer(career, compatibility(career, scores), similarity(career, scores),
                        tieBreaker(career, scores)))
                .sorted(Comparator.comparingInt(RankedCareer::compatibility).reversed()
                        .thenComparing(Comparator.comparingDouble(RankedCareer::similarity).reversed())
                        .thenComparing(Comparator.comparingInt(RankedCareer::tieBreaker).reversed())
                        .thenComparing(ranked -> ranked.career().getName()))
                .limit(limit)
                .toList();
    }
}
