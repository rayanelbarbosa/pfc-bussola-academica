package br.com.bussolaacademica.dto;

/** Vídeo do YouTube relacionado a uma área/curso. */
public record VideoResponse(String videoId, String title, String channelTitle, String thumbnailUrl, String url) {
}
