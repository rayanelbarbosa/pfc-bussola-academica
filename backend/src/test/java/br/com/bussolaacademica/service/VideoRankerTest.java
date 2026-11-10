package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.integration.youtube.VideoCandidate;
import br.com.bussolaacademica.model.RiasecCategory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class VideoRankerTest {

    private final VideoRanker ranker = new VideoRanker();

    private static VideoCandidate video(String id, String title, String description, long views, long likes, long seconds) {
        return new VideoCandidate(new VideoResponse(id, title, "Canal", null, "https://www.youtube.com/watch?v=" + id),
                description, views, likes, seconds);
    }

    private static List<String> ids(List<VideoResponse> videos) {
        return videos.stream().map(VideoResponse::videoId).toList();
    }

    @Test
    void relevantVideoBeatsPopularButOffTopicVideo() {
        List<VideoCandidate> candidates = List.of(
                video("meme", "Reagindo a vídeos engraçados", "compilado de memes", 9_000_000, 300_000, 600),
                video("rotina", "Rotina de quem faz Medicina: como é a profissão", "o dia a dia do curso e do trabalho", 40_000, 2_000, 720));

        assertThat(ids(ranker.rank(candidates, "Medicina", List.of(), 3))).containsExactly("rotina", "meme");
    }

    @Test
    void popularityBreaksTiesBetweenEquallyRelevantVideos() {
        List<VideoCandidate> candidates = List.of(
                video("pouco", "Medicina: como é a profissão", "rotina", 1_000, 50, 600),
                video("muito", "Medicina: como é a profissão", "rotina", 500_000, 25_000, 600));

        assertThat(ids(ranker.rank(candidates, "Medicina", List.of(), 3))).containsExactly("muito", "pouco");
    }

    @Test
    void studentProfileChangesTheOrder() {
        List<VideoCandidate> candidates = List.of(
                video("pesquisa", "Medicina e pesquisa: ciência no laboratório", "estudo e análise de dados", 50_000, 2_000, 600),
                video("pacientes", "Medicina: cuidar de pessoas e ajudar pacientes", "atendimento e comunidade", 50_000, 2_000, 600));

        assertThat(ids(ranker.rank(candidates, "Medicina", List.of(RiasecCategory.INVESTIGATIVE), 1)))
                .containsExactly("pesquisa");
        assertThat(ids(ranker.rank(candidates, "Medicina", List.of(RiasecCategory.SOCIAL), 1)))
                .containsExactly("pacientes");
    }

    @Test
    void dropsShortsAndPenalizesVeryLongVideos() {
        List<VideoCandidate> candidates = List.of(
                video("short", "Medicina carreira profissão rotina", "", 900_000, 90_000, 45),
                video("live", "Medicina carreira profissão rotina", "", 100_000, 5_000, 3 * 3600),
                video("normal", "Medicina carreira profissão rotina", "", 100_000, 5_000, 900));

        List<String> result = ids(ranker.rank(candidates, "Medicina", List.of(), 3));

        assertThat(result).containsExactly("normal", "live");
    }

    @Test
    void limitsTheNumberOfVideosAndHandlesEmptyInput() {
        List<VideoCandidate> candidates = List.of(
                video("a", "Direito profissão", "", 10, 1, 300),
                video("b", "Direito carreira", "", 20, 1, 300),
                video("c", "Direito rotina", "", 30, 1, 300));

        assertThat(ranker.rank(candidates, "Direito", null, 2)).hasSize(2);
        assertThat(ranker.rank(List.of(), "Direito", List.of(), 3)).isEmpty();
    }

    @Test
    void tokenizeRemovesAccentsCaseAndStopwords() {
        assertThat(VideoRanker.tokenize("Ciência da Computação: O DIA a dia!"))
                .containsExactly("ciencia", "computacao", "dia", "dia");
        assertThat(VideoRanker.tokenize(null)).isEmpty();
    }

    @Test
    void bm25RewardsTheDocumentWithMoreQueryTerms() {
        double[] scores = VideoRanker.bm25(List.of(
                List.of("medicina", "rotina", "profissao"),
                List.of("medicina", "memes", "humor")), Set.of("rotina", "profissao"));

        assertThat(scores[0]).isGreaterThan(scores[1]);
        assertThat(scores[1]).isZero();
    }
}
