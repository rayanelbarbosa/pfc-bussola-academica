import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { ForgotPasswordComponent } from './forgot-password.component';
import { environment } from '../../../../environments/environment';

describe('ForgotPasswordComponent', () => {
  let fixture: ComponentFixture<ForgotPasswordComponent>;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ForgotPasswordComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ForgotPasswordComponent);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  it('does not call the API with an invalid e-mail', () => {
    fixture.componentInstance.form.setValue({ email: 'nao-e-email' });
    fixture.componentInstance.submit();
    http.expectNone(`${environment.apiUrl}/auth/password/forgot`);
  });

  it('shows the neutral message returned by the API', () => {
    fixture.componentInstance.form.setValue({ email: 'aluna@teste.com' });
    fixture.componentInstance.submit();

    const req = http.expectOne(`${environment.apiUrl}/auth/password/forgot`);
    expect(req.request.body).toEqual({ email: 'aluna@teste.com' });
    req.flush({ message: 'Se o e-mail estiver cadastrado, você vai receber um link.' });
    fixture.detectChanges();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Se o e-mail estiver cadastrado');
  });
});
