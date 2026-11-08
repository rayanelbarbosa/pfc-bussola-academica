import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Dashboard } from '../models/dashboard.model';

/** Indicadores de uso da plataforma (somente administrador). */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  getMetrics(days: number): Observable<Dashboard> {
    return this.http.get<Dashboard>(`${environment.apiUrl}/admin/dashboard`, { params: new HttpParams().set('days', days) });
  }
}
