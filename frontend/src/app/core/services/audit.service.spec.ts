import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AuditService } from './audit.service';

describe('AuditService', () => {
  let service: AuditService;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/admin/audit-logs`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuditService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends action and trimmed e-mail only when they were filled in', () => {
    service.search({ action: 'LOGIN_FAILURE', email: ' a@b.com ', page: 2, size: 20 }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('action')).toBe('LOGIN_FAILURE');
    expect(req.request.params.get('email')).toBe('a@b.com');
    expect(req.request.params.get('page')).toBe('2');
    req.flush({ content: [], page: 2, size: 20, totalElements: 0, totalPages: 0 });

    service.search({ email: '  ', page: 0, size: 20 }).subscribe();
    const empty = http.expectOne((r) => r.url === url);
    expect(empty.request.params.keys()).toEqual(['page', 'size']);
    empty.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });
});
