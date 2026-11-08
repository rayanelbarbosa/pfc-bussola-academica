import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Option } from '../models/admission-method.model';
import { Question, SubmitTestRequest, VocationalTestResult } from '../models/vocational-test.model';

/** Comunicação com a API do teste vocacional (regra de negócio: pontuação RIASEC). */
@Injectable({ providedIn: 'root' })
export class VocationalTestService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/vocational-test`;

  getQuestions(): Observable<Question[]> {
    return this.http.get<Question[]>(`${this.baseUrl}/questions`);
  }

  getInterestAreas(): Observable<Option[]> {
    return this.http.get<Option[]>(`${this.baseUrl}/interest-areas`);
  }

  submitAnswers(request: SubmitTestRequest): Observable<VocationalTestResult> {
    return this.http.post<VocationalTestResult>(`${this.baseUrl}/results`, request);
  }

  getResult(id: number): Observable<VocationalTestResult> {
    return this.http.get<VocationalTestResult>(`${this.baseUrl}/results/${id}`);
  }
}
