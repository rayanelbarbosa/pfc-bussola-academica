import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { TesteVocacionalService } from './teste-vocacional.service';
import { Pergunta, ResultadoTesteResponse } from './models/teste-vocacional.models';

describe('TesteVocacionalService', () => {
  let service: TesteVocacionalService;
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080/api/teste-vocacional';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TesteVocacionalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve buscar as perguntas do teste vocacional via GET', () => {
    const perguntasMock: Pergunta[] = [
      { id: 1, enunciado: 'Gosto de construir coisas.', categoria: 'REALISTA' }
    ];

    service.listarPerguntas().subscribe((perguntas) => {
      expect(perguntas).toEqual(perguntasMock);
    });

    const req = httpMock.expectOne(`${baseUrl}/perguntas`);
    expect(req.request.method).toBe('GET');
    req.flush(perguntasMock);
  });

  it('deve enviar as respostas via POST e retornar o resultado calculado', () => {
    const resultadoMock: ResultadoTesteResponse = {
      id: 1,
      escoresPorCategoria: { Realista: 10, Investigativo: 2, Artístico: 2, Social: 2, Empreendedor: 2, Convencional: 2 },
      categoriasPrincipais: ['Realista'],
      areasRecomendadas: ['Engenharias', 'Tecnologia da Informação', 'Agronomia']
    };

    service.submeterTeste({ respostas: [{ perguntaId: 1, nota: 5 }] }).subscribe((resultado) => {
      expect(resultado).toEqual(resultadoMock);
    });

    const req = httpMock.expectOne(baseUrl);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ respostas: [{ perguntaId: 1, nota: 5 }] });
    req.flush(resultadoMock);
  });
});
