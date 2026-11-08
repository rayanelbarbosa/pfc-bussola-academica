package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.DashboardResponse;
import br.com.bussolaacademica.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Indicadores de uso da plataforma (RF15): rota /api/admin/**, liberada só para o perfil ADMIN. */
@RestController
@RequestMapping("/api/admin/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping
    public DashboardResponse metrics(@RequestParam(defaultValue = "30") int days) {
        return service.metrics(days);
    }
}
