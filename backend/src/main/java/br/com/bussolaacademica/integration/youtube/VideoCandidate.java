package br.com.bussolaacademica.integration.youtube;

import br.com.bussolaacademica.dto.VideoResponse;

/**
 * Vídeo candidato devolvido pelo YouTube, com o que o ranking usa: texto (título e descrição),
 * popularidade (visualizações e curtidas) e duração. Só o {@link VideoResponse} vai para o front-end.
 */
public record VideoCandidate(VideoResponse video, String description, long views, long likes, long durationSeconds) {
}
