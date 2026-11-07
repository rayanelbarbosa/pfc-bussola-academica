package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AdmissionMethodRequest;
import br.com.bussolaacademica.dto.AdmissionMethodResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.dto.StatusRequest;
import br.com.bussolaacademica.model.AdmissionType;
import br.com.bussolaacademica.service.AdmissionMethodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Gestão das formas de ingresso pelo administrador (RF11): inclusão, alteração, consulta e desativação. */
@RestController
@RequestMapping("/api/admin/admission-methods")
public class AdminAdmissionMethodController {

    private final AdmissionMethodService service;

    public AdminAdmissionMethodController(AdmissionMethodService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AdmissionMethodResponse> search(@RequestParam(required = false) String name,
                                                        @RequestParam(required = false) AdmissionType type,
                                                        @RequestParam(required = false) Boolean active,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return service.search(name, type, active, page, size);
    }

    @GetMapping("/{id}")
    public AdmissionMethodResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdmissionMethodResponse create(@Valid @RequestBody AdmissionMethodRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public AdmissionMethodResponse update(@PathVariable Long id, @Valid @RequestBody AdmissionMethodRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public AdmissionMethodResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.changeStatus(id, request.active());
    }
}
