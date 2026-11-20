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

  it('asks for the whole period when there is no filter', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(DashboardService).getMetrics(null, { from: null, to: null }).subscribe();
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/admin/dashboard`);
    expect(req.request.params.keys()).toEqual([]);
    req.flush({});
    http.verify();
  });

  it('sends the calendar dates instead of the number of days', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    TestBed.inject(DashboardService).getMetrics(30, { from: '2026-11-01', to: '2026-11-20' }).subscribe();
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/admin/dashboard`);
    expect(req.request.params.get('from')).toBe('2026-11-01');
    expect(req.request.params.get('to')).toBe('2026-11-20');
    expect(req.request.params.has('days')).toBeFalse();
    req.flush({});
    http.verify();
  });
});
