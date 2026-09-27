import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuditLog, Page } from '../models/audit.model';

export interface AuditFilter {
  action?: string;
  email?: string;
  page: number;
  size: number;
}

/** Consulta dos logs de auditoria (somente administrador). */
@Injectable({ providedIn: 'root' })
export class AuditService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/admin/audit-logs`;

  search(filter: AuditFilter): Observable<Page<AuditLog>> {
    let params = new HttpParams().set('page', filter.page).set('size', filter.size);
    if (filter.action) {
      params = params.set('action', filter.action);
    }
    if (filter.email?.trim()) {
      params = params.set('email', filter.email.trim());
    }
    return this.http.get<Page<AuditLog>>(this.baseUrl, { params });
  }
}
