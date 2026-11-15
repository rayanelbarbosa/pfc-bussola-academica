package br.com.bussolaacademica.service;

import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Regra de negócio: recomendação de carreiras pela concordância da primeira letra do código de Holland.
 *
 * <ol>
 *   <li>Cada carreira tem um tipo principal e um secundário, vindos do O*NET (ex.: Psicologia = Social e
 *       Investigativo).</li>
 *   <li>Fica na frente a carreira em que o estudante fez mais pontos no tipo principal dela. Empate: mais pontos no
 *       tipo secundário. Persistindo, ordem alfabética.</li>
 *   <li>Recomendadas: as {@value #LIMIT} primeiras das áreas escolhidas ("Ainda não sei" = todas as áreas).</li>
 *   <li>Aviso de área: se nenhuma carreira das áreas escolhidas tem como tipo principal um tipo do perfil do
 *       estudante, ele recebe também as {@value #LIMIT} primeiras de todas as áreas.</li>
 * </ol>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 4;

    /** Carreiras recomendadas e, quando há aviso de área, as que mais combinam em todas as áreas (senão, vazia). */
    public record Recommendation(List<Career> careers, List<Career> suggestions) {
    }

    public Recommendation recommend(List<InterestArea> areas, List<Career> careers,
                                    Map<RiasecCategory, Integer> scores, List<RiasecCategory> profile) {
        List<Career> ranking = rank(careers, scores);

        // "Ainda não sei" (ou resultado antigo sem área): todas as áreas, sem aviso
        if (areas.isEmpty() || areas.contains(InterestArea.UNDECIDED)) {
            return new Recommendation(top(ranking), List.of());
        }

        List<Career> chosen = ranking.stream().filter(career -> areas.contains(career.getArea())).toList();
        boolean combinesWithProfile = chosen.stream().anyMatch(career -> profile.contains(career.getPrimaryCategory()));
        return new Recommendation(top(chosen), combinesWithProfile ? List.of() : top(ranking));
    }

    /** Ordena: pontos no tipo principal da carreira, pontos no tipo secundário e nome. */
    List<Career> rank(List<Career> careers, Map<RiasecCategory, Integer> scores) {
        Comparator<Career> order = Comparator
                .comparing((Career career) -> scores.get(career.getPrimaryCategory()), Comparator.reverseOrder())
                .thenComparing(career -> scores.get(career.getSecondaryCategory()), Comparator.reverseOrder())
                .thenComparing(Career::getName);
        return careers.stream().sorted(order).toList();
    }

    private static List<Career> top(List<Career> careers) {
        return careers.stream().limit(LIMIT).toList();
    }
}
