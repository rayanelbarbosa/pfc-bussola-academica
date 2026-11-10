package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.CareerRequest;
import br.com.bussolaacademica.dto.CareerResponse;
import br.com.bussolaacademica.dto.OptionResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.dto.StatusRequest;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.service.CareerService;
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

import java.util.List;

/** Gestão das carreiras pelo administrador: inclusão, alteração, consulta e desativação. */
@RestController
@RequestMapping("/api/admin/careers")
public class AdminCareerController {

    private final CareerService service;

    public AdminCareerController(CareerService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<CareerResponse> search(@RequestParam(required = false) String name,
                                               @RequestParam(required = false) InterestArea area,
                                               @RequestParam(required = false) Boolean active,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return service.search(name, area, active, page, size);
    }

    @GetMapping("/areas")
    public List<OptionResponse> areas() {
        return service.areas();
    }

    @GetMapping("/categories")
    public List<OptionResponse> categories() {
        return service.categories();
    }

    @GetMapping("/{id}")
    public CareerResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CareerResponse create(@Valid @RequestBody CareerRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public CareerResponse update(@PathVariable Integer id, @Valid @RequestBody CareerRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public CareerResponse changeStatus(@PathVariable Integer id, @Valid @RequestBody StatusRequest request) {
        return service.changeStatus(id, request.active());
    }
}
