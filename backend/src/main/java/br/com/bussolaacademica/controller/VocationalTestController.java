package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.QuestionResponse;
import br.com.bussolaacademica.dto.SubmitTestRequest;
import br.com.bussolaacademica.dto.VocationalTestResultResponse;
import br.com.bussolaacademica.service.VocationalTestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Endpoints REST do teste vocacional. Só recebe/devolve dados; a regra fica no service. */
@RestController
@RequestMapping("/api/vocational-test")
public class VocationalTestController {

    private final VocationalTestService service;

    public VocationalTestController(VocationalTestService service) {
        this.service = service;
    }

    @GetMapping("/questions")
    public List<QuestionResponse> listQuestions() {
        return service.listQuestions();
    }

    @PostMapping("/results")
    @ResponseStatus(HttpStatus.CREATED)
    public VocationalTestResultResponse submit(@Valid @RequestBody SubmitTestRequest request) {
        return service.submit(request);
    }

    @GetMapping("/results/{id}")
    public VocationalTestResultResponse findResult(@PathVariable Long id) {
        return service.findResult(id);
    }
}
