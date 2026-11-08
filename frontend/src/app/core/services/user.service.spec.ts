import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { environment } from '../../../environments/environment';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let http: HttpTestingController;
  const url = `${environment.apiUrl}/users/me`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(UserService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('reads the user, the results and exports the data as a file', () => {
    service.getMe().subscribe();
    http.expectOne(url).flush({});

    service.getMyResults().subscribe();
    http.expectOne(`${url}/results`).flush([]);

    service.exportMyData().subscribe((blob) => expect(blob instanceof Blob).toBeTrue());
    const exp = http.expectOne(`${url}/export`);
    expect(exp.request.responseType).toBe('blob');
    exp.flush(new Blob(['{}'], { type: 'application/json' }));
  });

  it('deletes the own account', () => {
    service.deleteMyAccount().subscribe();
    const req = http.expectOne(url);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });
});
