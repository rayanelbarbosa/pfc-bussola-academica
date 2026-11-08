import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';

import { ResetPasswordComponent } from './reset-password.component';
import { environment } from '../../../../environments/environment';

describe('ResetPasswordComponent', () => {
  let fixture: ComponentFixture<ResetPasswordComponent>;
  let http: HttpTestingController;
  let router: Router;

  const validateUrl = `${environment.apiUrl}/auth/password/reset/validate`;
  const resetUrl = `${environment.apiUrl}/auth/password/reset`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ResetPasswordComponent],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    });
    http = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    fixture = TestBed.createComponent(ResetPasswordComponent);
  });

  afterEach(() => http.verify());

  function openWithToken(valid: boolean): void {
    fixture.componentRef.setInput('token', 'abc');
    fixture.detectChanges();
    const req = http.expectOne(validateUrl);
    expect(req.request.body).toEqual({ token: 'abc' });
    if (valid) {
      req.flush(null, { status: 204, statusText: 'No Content' });
    } else {
      req.flush({ status: 422, message: 'Este link de redefinição é inválido ou expirou.' }, { status: 422, statusText: 'Unprocessable' });
    }
    fixture.detectChanges();
  }

  const text = () => (fixture.nativeElement as HTMLElement).textContent ?? '';

  it('warns when the link has no token', () => {
    fixture.detectChanges();
    expect(text()).toContain('Link incompleto');
  });

  it('shows the expired message right away and hides the form for a used link', () => {
    openWithToken(false);
    expect(text()).toContain('inválido ou expirou');
    expect((fixture.nativeElement as HTMLElement).querySelector('form')).toBeNull();
  });

  it('requires matching passwords with letters and numbers', () => {
    openWithToken(true);
    const component = fixture.componentInstance;

    component.form.setValue({ password: 'semnumero', confirmPassword: 'semnumero' });
    component.submit();
    component.form.setValue({ password: 'Senha1234', confirmPassword: 'Senha9999' });
    component.submit();

    http.expectNone(resetUrl);
  });

  it('saves the new password and sends the user to the login', () => {
    openWithToken(true);
    fixture.componentInstance.form.setValue({ password: 'NovaSenha2026', confirmPassword: 'NovaSenha2026' });

    fixture.componentInstance.submit();
    const req = http.expectOne(resetUrl);
    expect(req.request.body).toEqual({ token: 'abc', newPassword: 'NovaSenha2026' });
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(router.navigate).toHaveBeenCalledWith(['/login'], { queryParams: { senhaRedefinida: 1 } });
  });
});
