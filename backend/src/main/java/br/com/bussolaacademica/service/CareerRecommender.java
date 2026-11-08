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
 *   <li>Cada carreira tem um código RIASEC de 2 letras (tipo dominante + tipo secundário).</li>
 *   <li>Compatibilidade = 2 × escore do tipo dominante + escore do tipo secundário
 *       (o tipo dominante pesa o dobro; vai de 6 a 30 pontos).</li>
 *   <li>Para cada área escolhida, recomenda as {@value #LIMIT} carreiras de maior compatibilidade;
 *       empates na última posição entram todos.</li>
 *   <li>"Ainda não sei": o ranking considera as carreiras de todas as áreas.</li>
 * </ol>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 3;

    public record RankedCareer(Career career, int compatibility) {
    }

    public static int compatibility(Career career, Map<RiasecCategory, Integer> scores) {
        return 2 * scores.get(career.getPrimaryCategory()) + scores.get(career.getSecondaryCategory());
    }

    /** Recomendações agrupadas por área, na ordem das áreas escolhidas. */
    public Map<InterestArea, List<RankedCareer>> recommend(List<InterestArea> areas, List<Career> careers,
                                                          Map<RiasecCategory, Integer> scores) {
        Map<InterestArea, List<RankedCareer>> result = new LinkedHashMap<>();
        for (InterestArea area : areas) {
            List<Career> candidates = area == InterestArea.UNDECIDED
                    ? careers
                    : careers.stream().filter(career -> career.getArea() == area).toList();
            result.put(area, top(candidates, scores));
        }
        return result;
    }

    List<RankedCareer> top(List<Career> candidates, Map<RiasecCategory, Integer> scores) {
        List<RankedCareer> ranked = candidates.stream()
                .map(career -> new RankedCareer(career, compatibility(career, scores)))
                .sorted(Comparator.comparingInt(RankedCareer::compatibility).reversed()
                        .thenComparing(ranked1 -> ranked1.career().getName()))
                .toList();
        if (ranked.size() <= LIMIT) {
            return ranked;
        }
        int cutoff = ranked.get(LIMIT - 1).compatibility();
        return ranked.stream().filter(item -> item.compatibility() >= cutoff).toList();
    }
}
