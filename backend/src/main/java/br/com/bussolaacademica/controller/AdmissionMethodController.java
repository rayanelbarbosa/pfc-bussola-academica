package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AdmissionMethodResponse;
import br.com.bussolaacademica.dto.OptionResponse;
import br.com.bussolaacademica.model.AdmissionType;
import br.com.bussolaacademica.service.AdmissionMethodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Consulta pública das formas de ingresso (RF01): não exige login. */
@RestController
@RequestMapping("/api/admission-methods")
public class AdmissionMethodController {

    private final AdmissionMethodService service;

    public AdmissionMethodController(AdmissionMethodService service) {
        this.service = service;
    }

    @GetMapping
    public List<AdmissionMethodResponse> listActive(@RequestParam(required = false) AdmissionType type) {
        return service.listActive(type);
    }

    @GetMapping("/types")
    public List<OptionResponse> types() {
        return service.types();
    }
}
