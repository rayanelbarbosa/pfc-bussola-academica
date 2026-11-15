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
 *   <li>Cada carreira tem um código de Holland vindo do O*NET: tipo principal, secundário e, quase sempre, um 3º tipo
 *       (ex.: Psicologia = Social, Investigativo e Convencional).</li>
 *   <li>Fica na frente a carreira em que o estudante fez mais pontos no tipo principal dela. Empate: mais pontos no
 *       tipo secundário, depois no 3º tipo (carreira sem 3º tipo conta 0). Persistindo, ordem alfabética.</li>
 *   <li>Recomendadas: as {@value #LIMIT} primeiras das áreas escolhidas ("Ainda não sei" = todas as áreas).</li>
 *   <li>Aviso de área: se nenhuma carreira das áreas escolhidas tem como tipo principal um tipo do perfil do
 *       estudante, ele recebe também as {@value #LIMIT} primeiras de todas as áreas.</li>
 *   <li>Ranking completo: todas as carreiras, de todas as áreas, na mesma ordem. Carreiras com o código igual empatam
 *       e seguem a ordem alfabética, então algumas nunca chegariam às 4 primeiras; com o ranking completo, todas podem
 *       ser vistas.</li>
 * </ol>
 */
@Component
public class CareerRecommender {

    static final int LIMIT = 4;

    /**
     * Carreiras recomendadas; quando há aviso de área, as que mais combinam em todas as áreas (senão, vazia); e o
     * ranking completo de todas as áreas.
     */
    public record Recommendation(List<Career> careers, List<Career> suggestions, List<Career> ranking) {
    }

    public Recommendation recommend(List<InterestArea> areas, List<Career> careers,
                                    Map<RiasecCategory, Integer> scores, List<RiasecCategory> profile) {
        List<Career> ranking = rank(careers, scores);

        // "Ainda não sei" (ou resultado antigo sem área): todas as áreas, sem aviso
        if (areas.isEmpty() || areas.contains(InterestArea.UNDECIDED)) {
            return new Recommendation(top(ranking), List.of(), ranking);
        }

        List<Career> chosen = ranking.stream().filter(career -> areas.contains(career.getArea())).toList();
        boolean combinesWithProfile = chosen.stream().anyMatch(career -> profile.contains(career.getPrimaryCategory()));
        return new Recommendation(top(chosen), combinesWithProfile ? List.of() : top(ranking), ranking);
    }

    /** Ordena: pontos no 1º tipo da carreira, no 2º, no 3º e, por último, o nome. */
    List<Career> rank(List<Career> careers, Map<RiasecCategory, Integer> scores) {
        Comparator<Career> order = Comparator
                .comparing((Career career) -> scores.get(career.getPrimaryCategory()), Comparator.reverseOrder())
                .thenComparing(career -> scores.get(career.getSecondaryCategory()), Comparator.reverseOrder())
                .thenComparing(career -> thirdTypeScore(career, scores), Comparator.reverseOrder())
                .thenComparing(Career::getName);
        return careers.stream().sorted(order).toList();
    }

    /** Pontos do estudante no 3º tipo da carreira; 0 quando ela não tem 3º tipo. */
    static int thirdTypeScore(Career career, Map<RiasecCategory, Integer> scores) {
        return career.getTertiaryCategory() == null ? 0 : scores.get(career.getTertiaryCategory());
    }

    private static List<Career> top(List<Career> careers) {
        return careers.stream().limit(LIMIT).toList();
    }
}
