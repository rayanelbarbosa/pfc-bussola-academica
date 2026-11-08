package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.VideoResponse;
import br.com.bussolaacademica.service.VideoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Vídeos de apoio de uma carreira cadastrada, vindos da YouTube Data API v3 (integração externa). */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public List<VideoResponse> searchByCareer(@RequestParam String career) {
        return videoService.searchByCareer(career);
    }
}
