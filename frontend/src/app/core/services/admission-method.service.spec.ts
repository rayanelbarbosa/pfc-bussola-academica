import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AdmissionMethodService } from './admission-method.service';
import { AdmissionMethodRequest } from '../models/admission-method.model';

describe('AdmissionMethodService', () => {
  let service: AdmissionMethodService;
  let http: HttpTestingController;
  const publicUrl = `${environment.apiUrl}/admission-methods`;
  const adminUrl = `${environment.apiUrl}/admin/admission-methods`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AdmissionMethodService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists the active methods and the types on the public endpoints', () => {
    service.listActive().subscribe();
    http.expectOne(publicUrl).flush([]);
    service.types().subscribe();
    http.expectOne(`${publicUrl}/types`).flush([]);
  });

  it('searches only with the filters that were filled in (name trimmed)', () => {
    service.search({ name: '  Enem ', type: 'NATIONAL_EXAM', active: 'true', page: 1, size: 20 }).subscribe();
    const req = http.expectOne((r) => r.url === adminUrl);
    expect(req.request.params.get('name')).toBe('Enem');
    expect(req.request.params.get('type')).toBe('NATIONAL_EXAM');
    expect(req.request.params.get('active')).toBe('true');
    expect(req.request.params.get('page')).toBe('1');
    req.flush({ content: [], page: 1, size: 20, totalElements: 0, totalPages: 0 });

    service.search({ name: '   ', page: 0, size: 20 }).subscribe();
    const empty = http.expectOne((r) => r.url === adminUrl);
    expect(empty.request.params.keys()).toEqual(['page', 'size']);
    empty.flush({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
  });

  it('creates, updates and changes the status', () => {
    const body = { name: 'Novo' } as AdmissionMethodRequest;
    service.create(body).subscribe();
    const create = http.expectOne(adminUrl);
    expect(create.request.method).toBe('POST');
    create.flush({});

    service.update(7, body).subscribe();
    const update = http.expectOne(`${adminUrl}/7`);
    expect(update.request.method).toBe('PUT');
    update.flush({});

    service.changeStatus(7, false).subscribe();
    const status = http.expectOne(`${adminUrl}/7/status`);
    expect(status.request.method).toBe('PATCH');
    expect(status.request.body).toEqual({ active: false });
    status.flush({});
  });
});
