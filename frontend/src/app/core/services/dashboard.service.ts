import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Dashboard } from '../models/dashboard.model';
import { DateRangeValue } from '../models/date-range.model';

/** Indicadores de uso da plataforma (somente administrador). */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly http = inject(HttpClient);

  /**
   * Com um período do calendário (data inicial e final), usa as datas; senão, com "days", os últimos 7/30/90 dias;
   * sem nenhum dos dois, todo o período.
   */
  getMetrics(days: number | null, range?: DateRangeValue): Observable<Dashboard> {
    let params = new HttpParams();
    if (range?.from && range.to) {
      params = params.set('from', range.from).set('to', range.to);
    } else if (days) {
      params = params.set('days', days);
    }
    return this.http.get<Dashboard>(`${environment.apiUrl}/admin/dashboard`, { params });
  }
}
