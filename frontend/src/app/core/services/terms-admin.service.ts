import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { TermsVersion } from '../models/auth.model';

export type PublishTermsRequest = Pick<TermsVersion, 'version' | 'summary' | 'termsOfUse' | 'privacyPolicy'>;

/** Versões do Termo de Uso e da Política de Privacidade (área do administrador). */
@Injectable({ providedIn: 'root' })
export class TermsAdminService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/terms`;

  history(): Observable<TermsVersion[]> {
    return this.http.get<TermsVersion[]>(this.baseUrl);
  }

  publish(request: PublishTermsRequest): Observable<TermsVersion> {
    return this.http.post<TermsVersion>(this.baseUrl, request);
  }
}
