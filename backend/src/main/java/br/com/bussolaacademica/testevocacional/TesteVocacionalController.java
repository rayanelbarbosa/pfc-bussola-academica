package br.com.bussolaacademica.testevocacional;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teste-vocacional")
public class TesteVocacionalController {

    private final TesteVocacionalService service;

    public TesteVocacionalController(TesteVocacionalService service) {
        this.service = service;
    }

    @GetMapping("/perguntas")
    public List<Pergunta> listarPerguntas() {
        return service.listarPerguntas();
    }

    @PostMapping
    public ResponseEntity<ResultadoTesteResponse> submeterTeste(@Valid @RequestBody SubmeterTesteRequest request) {
        ResultadoTesteResponse resultado = service.calcularEPersistirResultado(request);
        return ResponseEntity.ok(resultado);
    }

    @ExceptionHandler(PerguntaInvalidaException.class)
    public ResponseEntity<Map<String, String>> tratarPerguntaInvalida(PerguntaInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erro", ex.getMessage()));
    }
}
