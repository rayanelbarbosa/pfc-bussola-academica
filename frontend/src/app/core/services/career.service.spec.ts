import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { CareerService } from './career.service';

describe('CareerService', () => {
  let service: CareerService;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/admin/careers`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(CareerService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sends only the filled filters', () => {
    service.search({ name: ' prompt ', area: 'EDUCATION', active: '', page: 1, size: 10 }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('name')).toBe('prompt');
    expect(req.request.params.get('area')).toBe('EDUCATION');
    expect(req.request.params.has('active')).toBeFalse();
    req.flush({ content: [], page: 1, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('calls the admin endpoints', () => {
    const body = { name: 'X', area: 'HEALTH', primaryCategory: 'SOCIAL', secondaryCategory: 'INVESTIGATIVE', tertiaryCategory: null, description: 'D', active: true };
    service.areas().subscribe();
    http.expectOne(`${url}/areas`).flush([]);
    service.categories().subscribe();
    http.expectOne(`${url}/categories`).flush([]);
    service.create(body).subscribe();
    expect(http.expectOne(url).request.method).toBe('POST');
    service.update(3, body).subscribe();
    expect(http.expectOne(`${url}/3`).request.method).toBe('PUT');
    service.changeStatus(3, false).subscribe();
    const status = http.expectOne(`${url}/3/status`);
    expect(status.request.method).toBe('PATCH');
    expect(status.request.body).toEqual({ active: false });
  });
});
