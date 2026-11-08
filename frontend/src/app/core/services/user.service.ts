import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ResultSummary, User } from '../models/auth.model';

/** Dados do próprio usuário (direitos do titular na LGPD). */
@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/users/me`;

  getMe(): Observable<User> {
    return this.http.get<User>(this.baseUrl);
  }

  getMyResults(): Observable<ResultSummary[]> {
    return this.http.get<ResultSummary[]>(`${this.baseUrl}/results`);
  }

  /** Arquivo JSON com todos os dados pessoais (RF10, portabilidade). */
  exportMyData(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, { responseType: 'blob' });
  }

  deleteMyAccount(): Observable<void> {
    return this.http.delete<void>(this.baseUrl);
  }
}
