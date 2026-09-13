import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting
} from '@angular/common/http/testing';

import { TesteVocacionalComponent } from './teste-vocacional.component';
import { Pergunta, ResultadoTesteResponse } from '../models/teste-vocacional.models';

describe('TesteVocacionalComponent', () => {
  let httpMock: HttpTestingController;
  const baseUrl = 'http://localhost:8080/api/teste-vocacional';

  const perguntasMock: Pergunta[] = [
    { id: 1, enunciado: 'Gosto de construir coisas.', categoria: 'REALISTA' },
    { id: 2, enunciado: 'Gosto de investigar.', categoria: 'INVESTIGATIVO' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TesteVocacionalComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve carregar as perguntas ao iniciar', () => {
    const fixture = TestBed.createComponent(TesteVocacionalComponent);
    fixture.detectChanges();

    const req = httpMock.expectOne(`${baseUrl}/perguntas`);
    req.flush(perguntasMock);

    expect(fixture.componentInstance.perguntas.length).toBe(2);
  });

  it('nao permite enviar antes de responder todas as perguntas', () => {
    const fixture = TestBed.createComponent(TesteVocacionalComponent);
    fixture.detectChanges();
    httpMock.expectOne(`${baseUrl}/perguntas`).flush(perguntasMock);

    fixture.componentInstance.enviarRespostas();

    expect(fixture.componentInstance.mensagemErro).toContain('Responda todas as perguntas');
    httpMock.expectNone(baseUrl);
  });

  it('exibe o resultado apos enviar todas as respostas', () => {
    const fixture = TestBed.createComponent(TesteVocacionalComponent);
    fixture.detectChanges();
    httpMock.expectOne(`${baseUrl}/perguntas`).flush(perguntasMock);

    fixture.componentInstance.notas = { 1: 5, 2: 3 };
    fixture.componentInstance.enviarRespostas();

    const resultadoMock: ResultadoTesteResponse = {
      id: 1,
      escoresPorCategoria: { Realista: 5, Investigativo: 3 },
      categoriasPrincipais: ['Realista'],
      areasRecomendadas: ['Engenharias']
    };
    httpMock.expectOne(baseUrl).flush(resultadoMock);

    expect(fixture.componentInstance.resultado).toEqual(resultadoMock);
  });
});
