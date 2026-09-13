import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Pergunta,
  ResultadoTesteResponse,
  SubmeterTesteRequest
} from './models/teste-vocacional.models';

/**
 * Servico responsavel por conversar com o back-end da regra de negocio
 * "pontuacao do teste vocacional" (RIASEC).
 */
@Injectable({ providedIn: 'root' })
export class TesteVocacionalService {
  private readonly baseUrl = 'http://localhost:8080/api/teste-vocacional';

  constructor(private readonly http: HttpClient) {}

  listarPerguntas(): Observable<Pergunta[]> {
    return this.http.get<Pergunta[]>(`${this.baseUrl}/perguntas`);
  }

  submeterTeste(request: SubmeterTesteRequest): Observable<ResultadoTesteResponse> {
    return this.http.post<ResultadoTesteResponse>(this.baseUrl, request);
  }
}
