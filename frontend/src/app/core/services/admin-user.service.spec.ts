import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { AdminUserService } from './admin-user.service';

describe('AdminUserService', () => {
  let service: AdminUserService;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/admin/users`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AdminUserService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('searches by text and role, ignoring empty filters', () => {
    service.search({ search: ' ana ', role: 'ADMIN', page: 0, size: 10 }).subscribe();
    const req = http.expectOne((r) => r.url === url);
    expect(req.request.params.get('search')).toBe('ana');
    expect(req.request.params.get('role')).toBe('ADMIN');
    req.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });

    service.search({ search: '', page: 0, size: 10 }).subscribe();
    const empty = http.expectOne((r) => r.url === url);
    expect(empty.request.params.has('search')).toBeFalse();
    expect(empty.request.params.has('role')).toBeFalse();
    empty.flush({ content: [], page: 0, size: 10, totalElements: 0, totalPages: 0 });
  });

  it('creates an admin, changes a role and deletes a user', () => {
    service.createAdmin({ name: 'Ana', email: 'ana@email.com', password: 'Senha@123' }).subscribe();
    const create = http.expectOne(url);
    expect(create.request.method).toBe('POST');
    create.flush({});

    service.changeRole(3, 'ADMIN').subscribe();
    const role = http.expectOne(`${url}/3/role`);
    expect(role.request.method).toBe('PATCH');
    expect(role.request.body).toEqual({ role: 'ADMIN' });
    role.flush({});

    service.deleteUser(3).subscribe();
    const del = http.expectOne(`${url}/3`);
    expect(del.request.method).toBe('DELETE');
    del.flush(null);
  });
});
