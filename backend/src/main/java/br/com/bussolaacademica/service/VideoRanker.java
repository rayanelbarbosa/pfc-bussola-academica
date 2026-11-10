package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.integration.youtube.VideoCandidate;
import br.com.bussolaacademica.model.RiasecCategory;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regra de negócio: escolhe os vídeos de uma carreira entre os candidatos que o YouTube devolveu.
 *
 * <p>O YouTube ordena só pela relevância da busca, que é sempre "nome da carreira + carreira profissão".
 * Aqui os candidatos são reordenados para cada estudante, com três sinais:</p>
 * <ol>
 *   <li><b>Relevância textual (peso 0,6)</b>: BM25 do título e da descrição contra os termos da carreira,
 *       termos de "como é a profissão" e palavras dos tipos RIASEC principais do estudante
 *       (ex.: perfil Social favorece vídeos que falam de pacientes, de ajudar e de atendimento).</li>
 *   <li><b>Popularidade (peso 0,3)</b>: log10 das visualizações, normalizado entre os candidatos
 *       (o log evita que um vídeo viral apague todos os outros).</li>
 *   <li><b>Aprovação (peso 0,1)</b>: curtidas por visualização.</li>
 * </ol>
 * <p>Vídeos com menos de 60 s (shorts) saem; vídeos com mais de 40 min (aulas e lives) perdem metade da nota;
 * vídeos que não citam nenhuma palavra do nome da carreira também perdem metade.</p>
 *
 * <p>Complexidade: O(V · (T + Q) + V log V), com V candidatos (15), T palavras por vídeo e Q termos da consulta.</p>
 */
@Component
public class VideoRanker {

    static final double K1 = 1.2;
    static final double B = 0.75;
    static final double RELEVANCE_WEIGHT = 0.6;
    static final double POPULARITY_WEIGHT = 0.3;
    static final double APPROVAL_WEIGHT = 0.1;
    static final long MIN_SECONDS = 60;
    static final long LONG_SECONDS = 40 * 60;

    private static final Set<String> STOPWORDS = Set.of(
            "a", "o", "as", "os", "de", "da", "do", "das", "dos", "e", "em", "no", "na", "nos", "nas", "um", "uma",
            "para", "pra", "com", "por", "que", "se", "ao", "aos", "mais", "como", "sobre", "sua", "seu", "voce");

    /** Termos de "como é a profissão": favorecem vídeos que mostram a carreira, não aulas ou memes. */
    private static final List<String> CAREER_TERMS = List.of(
            "profissao", "carreira", "rotina", "curso", "faculdade", "trabalho", "mercado", "dia");

    private static final Map<RiasecCategory, List<String>> PROFILE_TERMS = new EnumMap<>(Map.of(
            RiasecCategory.REALISTIC, List.of("pratica", "campo", "obra", "maquina", "ferramenta", "construir", "tecnica"),
            RiasecCategory.INVESTIGATIVE, List.of("pesquisa", "ciencia", "laboratorio", "analise", "estudo", "dados", "investigacao"),
            RiasecCategory.ARTISTIC, List.of("criatividade", "criar", "arte", "design", "projeto", "portfolio", "criativo"),
            RiasecCategory.SOCIAL, List.of("pessoas", "ajudar", "cuidar", "ensinar", "paciente", "atendimento", "comunidade"),
            RiasecCategory.ENTERPRISING, List.of("negocio", "lideranca", "empreender", "salario", "gestao", "vendas", "empresa"),
            RiasecCategory.CONVENTIONAL, List.of("organizacao", "processo", "planejamento", "controle", "documento", "financas", "detalhe")));

    public record RankedVideo(VideoResponse video, double score) {
    }

    /** Os {@code limit} melhores vídeos para a carreira e o perfil (perfil vazio = só carreira e popularidade). */
    public List<VideoResponse> rank(Collection<VideoCandidate> candidates, String careerName,
                                    List<RiasecCategory> profile, int limit) {
        return score(candidates, careerName, profile).stream()
                .limit(limit)
                .map(RankedVideo::video)
                .toList();
    }

    List<RankedVideo> score(Collection<VideoCandidate> candidates, String careerName, List<RiasecCategory> profile) {
        List<VideoCandidate> pool = candidates.stream()
                .filter(candidate -> candidate.durationSeconds() == 0 || candidate.durationSeconds() >= MIN_SECONDS)
                .toList();
        if (pool.isEmpty()) {
            return List.of();
        }

        Set<String> careerTokens = new LinkedHashSet<>(tokenize(careerName));
        Set<String> queryTerms = new LinkedHashSet<>(careerTokens);
        queryTerms.addAll(CAREER_TERMS);
        for (RiasecCategory category : profile == null ? List.<RiasecCategory>of() : profile) {
            queryTerms.addAll(PROFILE_TERMS.getOrDefault(category, List.of()));
        }

        // O título conta duas vezes: é o que o estudante lê antes de clicar
        List<List<String>> documents = new ArrayList<>(pool.size());
        for (VideoCandidate candidate : pool) {
            String title = candidate.video().title() == null ? "" : candidate.video().title();
            List<String> words = new ArrayList<>(tokenize(title));
            words.addAll(tokenize(title));
            words.addAll(tokenize(candidate.description()));
            documents.add(words);
        }

        double[] relevance = bm25(documents, queryTerms);
        double maxRelevance = Arrays.stream(relevance).max().orElse(0);
        double maxPopularity = pool.stream().mapToDouble(VideoRanker::popularity).max().orElse(0);

        List<RankedVideo> ranked = new ArrayList<>(pool.size());
        for (int i = 0; i < pool.size(); i++) {
            VideoCandidate candidate = pool.get(i);
            double textual = maxRelevance > 0 ? relevance[i] / maxRelevance : 0;
            double popular = maxPopularity > 0 ? popularity(candidate) / maxPopularity : 0;
            double approval = candidate.views() > 0 ? Math.min(1.0, (double) candidate.likes() / candidate.views() * 20) : 0;
            double score = RELEVANCE_WEIGHT * textual + POPULARITY_WEIGHT * popular + APPROVAL_WEIGHT * approval;
            if (candidate.durationSeconds() > LONG_SECONDS) {
                score *= 0.5;
            }
            if (!careerTokens.isEmpty() && new HashSet<>(documents.get(i)).stream().noneMatch(careerTokens::contains)) {
                score *= 0.5;
            }
            ranked.add(new RankedVideo(candidate.video(), score));
        }
        ranked.sort(Comparator.comparingDouble(RankedVideo::score).reversed());
        return ranked;
    }

    /**
     * BM25 (Robertson e Zaragoza): para cada termo da consulta, soma IDF × frequência saturada,
     * corrigida pelo tamanho do documento. O IDF é calculado sobre os próprios candidatos.
     */
    static double[] bm25(List<List<String>> documents, Set<String> queryTerms) {
        int n = documents.size();
        double averageLength = documents.stream().mapToInt(List::size).average().orElse(0);
        List<Map<String, Integer>> frequencies = new ArrayList<>(n);
        Map<String, Integer> documentFrequency = new HashMap<>();
        for (List<String> document : documents) {
            Map<String, Integer> tf = new HashMap<>();
            for (String word : document) {
                tf.merge(word, 1, Integer::sum);
            }
            frequencies.add(tf);
            for (String word : tf.keySet()) {
                documentFrequency.merge(word, 1, Integer::sum);
            }
        }
        double[] scores = new double[n];
        for (String term : queryTerms) {
            int df = documentFrequency.getOrDefault(term, 0);
            if (df == 0) {
                continue;
            }
            double idf = Math.log(1 + (n - df + 0.5) / (df + 0.5));
            for (int i = 0; i < n; i++) {
                int tf = frequencies.get(i).getOrDefault(term, 0);
                if (tf == 0) {
                    continue;
                }
                double length = documents.get(i).size();
                double norm = averageLength > 0 ? length / averageLength : 1;
                scores[i] += idf * (tf * (K1 + 1)) / (tf + K1 * (1 - B + B * norm));
            }
        }
        return scores;
    }

    private static double popularity(VideoCandidate candidate) {
        return Math.log10(candidate.views() + 1.0);
    }

    /** Minúsculas, sem acento, só letras e números, sem palavras vazias e sem palavras de 1 letra. */
    static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String plain = Normalizer.normalize(text.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return Arrays.stream(plain.split("[^a-z0-9]+"))
                .filter(word -> word.length() > 1 && !STOPWORDS.contains(word))
                .toList();
    }
}
