import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  it('asks for the metrics of the chosen period', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(DashboardService).getMetrics(30).subscribe();
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/admin/dashboard`);
    expect(req.request.params.get('days')).toBe('30');
    req.flush({});
    http.verify();
  });
});
