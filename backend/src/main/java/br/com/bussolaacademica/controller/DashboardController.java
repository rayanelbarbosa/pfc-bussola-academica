package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.DashboardResponse;
import br.com.bussolaacademica.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** Indicadores de uso da plataforma (RF15): rota /api/admin/**, liberada só para o perfil ADMIN. */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /**
     * Com {@code from} e/ou {@code to} (AAAA-MM-DD), usa o período do calendário (um dia só: as duas datas iguais).
     * Sem datas, usa o período rápido ({@code days}: 7, 30 ou 90). Sem nada, todo o período.
     */
    @GetMapping
    public DashboardResponse metrics(@RequestParam(required = false) Integer days,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        if (from != null || to != null) {
            return service.metrics(from, to);
        }
        return days != null ? service.metrics(days) : service.metricsAllTime();
    }
}
