import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { authResponse } from '../../../core/testing/auth-fixtures';

describe('LoginComponent', () => {
  let fixture: ComponentFixture<LoginComponent>;
  let auth: jasmine.SpyObj<AuthService>;
  let router: Router;

  beforeEach(async () => {
    auth = jasmine.createSpyObj('AuthService', ['login']);
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }]
    }).compileComponents();
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
  });

  it('does not call the API when the form is invalid', () => {
    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(auth.login).not.toHaveBeenCalled();
    expect(fixture.nativeElement.querySelectorAll('.field-error').length).toBe(2);
  });

  it('logs in and goes back to the requested page', () => {
    auth.login.and.returnValue(of(authResponse()));
    fixture.componentRef.setInput('voltar', '/teste-vocacional');
    fixture.componentInstance.form.setValue({ email: 'marina@teste.com', password: 'Senha1234' });

    fixture.componentInstance.submit();

    expect(auth.login).toHaveBeenCalledWith({ email: 'marina@teste.com', password: 'Senha1234' });
    expect(router.navigateByUrl).toHaveBeenCalledWith('/teste-vocacional');
  });

  it('ignores external return URLs', () => {
    auth.login.and.returnValue(of(authResponse()));
    fixture.componentRef.setInput('voltar', '//site-malicioso.com');
    fixture.componentInstance.form.setValue({ email: 'marina@teste.com', password: 'Senha1234' });

    fixture.componentInstance.submit();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('shows the API message on wrong credentials', () => {
    auth.login.and.returnValue(
      throwError(() => new HttpErrorResponse({ status: 401, error: { message: 'E-mail ou senha inválidos.' } }))
    );
    fixture.componentInstance.form.setValue({ email: 'marina@teste.com', password: 'errada123' });

    fixture.componentInstance.submit();
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('.error-message')?.textContent).toContain('E-mail ou senha inválidos.');
  });
});
