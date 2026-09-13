package br.com.bussolaacademica.testevocacional;

import java.util.List;
import java.util.Map;

/**
 * Resultado calculado do teste vocacional: o escore por categoria RIASEC,
 * a(s) categoria(s) com maior pontuacao e as areas/cursos recomendados.
 */
public record ResultadoTesteResponse(
        Long id,
        Map<String, Integer> escoresPorCategoria,
        List<String> categoriasPrincipais,
        List<String> areasRecomendadas
) {
}
