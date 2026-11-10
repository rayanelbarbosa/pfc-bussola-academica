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
 *   <li>Desempate: maior escore do estudante no 3º tipo da carreira; persistindo, ordem alfabética.</li>
 *   <li>Para cada área escolhida, recomenda exatamente as {@value #LIMIT} primeiras: poucas opções, bem
 *       ordenadas, ajudam mais quem está em dúvida do que uma lista longa de empatadas.</li>
 *   <li>"Ainda não sei": o ranking considera as carreiras de todas as áreas e mostra {@value #UNDECIDED_LIMIT}.</li>
 * </ol>
 *
 * <p>Complexidade: O(c log c) por área, por causa da ordenação das c carreiras candidatas.</p>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 3;
    static final int UNDECIDED_LIMIT = 5;

    public record RankedCareer(Career career, int compatibility, int tieBreaker) {
    }

    public static int compatibility(Career career, Map<RiasecCategory, Integer> scores) {
        return 2 * scores.get(career.getPrimaryCategory()) + scores.get(career.getSecondaryCategory());
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
                .map(career -> new RankedCareer(career, compatibility(career, scores), tieBreaker(career, scores)))
                .sorted(Comparator.comparingInt(RankedCareer::compatibility).reversed()
                        .thenComparing(Comparator.comparingInt(RankedCareer::tieBreaker).reversed())
                        .thenComparing(ranked -> ranked.career().getName()))
                .limit(limit)
                .toList();
    }
}
