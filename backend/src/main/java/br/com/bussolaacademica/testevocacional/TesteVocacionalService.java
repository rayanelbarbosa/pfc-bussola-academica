package br.com.bussolaacademica.testevocacional;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regra de negocio (RN) escolhida para a primeira entrega de codigo do PFC:
 * pontuacao do teste vocacional com base no modelo RIASEC (Holland).
 *
 * A regra: cada resposta do usuario (nota de 1 a 5) e somada ao escore da
 * categoria RIASEC da respectiva pergunta; a(s) categoria(s) com maior
 * escore total sao consideradas o perfil principal do usuario, e as areas
 * recomendadas sao a uniao das areas associadas a essa(s) categoria(s).
 */
@Service
public class TesteVocacionalService {

    private final ResultadoTesteRepository repository;
    private final Map<Integer, Pergunta> perguntasPorId;

    public TesteVocacionalService(ResultadoTesteRepository repository) {
        this.repository = repository;
        this.perguntasPorId = new LinkedHashMap<>();
        for (Pergunta pergunta : BancoDePerguntas.PERGUNTAS) {
            this.perguntasPorId.put(pergunta.id(), pergunta);
        }
    }

    public List<Pergunta> listarPerguntas() {
        return BancoDePerguntas.PERGUNTAS;
    }

    public ResultadoTesteResponse calcularEPersistirResultado(SubmeterTesteRequest request) {
        Map<CategoriaRiasec, Integer> escoresPorCategoria = calcularEscores(request);

        int escoreMaximo = escoresPorCategoria.values().stream()
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);

        List<CategoriaRiasec> categoriasPrincipais = new ArrayList<>();
        for (CategoriaRiasec categoria : CategoriaRiasec.values()) {
            if (escoresPorCategoria.get(categoria) == escoreMaximo) {
                categoriasPrincipais.add(categoria);
            }
        }

        Set<String> areasRecomendadas = new LinkedHashSet<>();
        for (CategoriaRiasec categoria : categoriasPrincipais) {
            areasRecomendadas.addAll(categoria.getAreasRecomendadas());
        }

        ResultadoTeste entidade = new ResultadoTeste(
                LocalDateTime.now(),
                escoresPorCategoria.get(CategoriaRiasec.REALISTA),
                escoresPorCategoria.get(CategoriaRiasec.INVESTIGATIVO),
                escoresPorCategoria.get(CategoriaRiasec.ARTISTICO),
                escoresPorCategoria.get(CategoriaRiasec.SOCIAL),
                escoresPorCategoria.get(CategoriaRiasec.EMPREENDEDOR),
                escoresPorCategoria.get(CategoriaRiasec.CONVENCIONAL),
                String.join(", ", categoriasPrincipais.stream().map(CategoriaRiasec::getNomeExibicao).toList()),
                String.join(", ", areasRecomendadas)
        );
        ResultadoTeste salvo = repository.save(entidade);

        Map<String, Integer> escoresPorNome = new LinkedHashMap<>();
        for (Map.Entry<CategoriaRiasec, Integer> entry : escoresPorCategoria.entrySet()) {
            escoresPorNome.put(entry.getKey().getNomeExibicao(), entry.getValue());
        }

        return new ResultadoTesteResponse(
                salvo.getId(),
                escoresPorNome,
                categoriasPrincipais.stream().map(CategoriaRiasec::getNomeExibicao).toList(),
                List.copyOf(areasRecomendadas)
        );
    }

    private Map<CategoriaRiasec, Integer> calcularEscores(SubmeterTesteRequest request) {
        Map<CategoriaRiasec, Integer> escores = new EnumMap<>(CategoriaRiasec.class);
        for (CategoriaRiasec categoria : CategoriaRiasec.values()) {
            escores.put(categoria, 0);
        }

        for (RespostaDTO resposta : request.respostas()) {
            Pergunta pergunta = perguntasPorId.get(resposta.perguntaId());
            if (pergunta == null) {
                throw new PerguntaInvalidaException(resposta.perguntaId());
            }
            escores.merge(pergunta.categoria(), resposta.nota(), Integer::sum);
        }

        return escores;
    }
}
