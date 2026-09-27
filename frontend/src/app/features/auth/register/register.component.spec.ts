import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { authResponse } from '../../../core/testing/auth-fixtures';

describe('RegisterComponent', () => {
  let fixture: ComponentFixture<RegisterComponent>;
  let auth: jasmine.SpyObj<AuthService>;

  const valid = {
    name: 'Marina Alves',
    email: 'marina@teste.com',
    password: 'Senha1234',
    confirmPassword: 'Senha1234',
    acceptedTerms: true
  };

  beforeEach(async () => {
    auth = jasmine.createSpyObj('AuthService', ['register']);
    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }]
    }).compileComponents();
    spyOn(TestBed.inject(Router), 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(RegisterComponent);
    fixture.detectChanges();
  });

  it('shows links to the Terms of Use and the Privacy Policy', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('a[href="/termos-de-uso"]')).toBeTruthy();
    expect(el.querySelector('a[href="/politica-de-privacidade"]')).toBeTruthy();
  });

  it('requires accepting the legal documents', () => {
    fixture.componentInstance.form.setValue({ ...valid, acceptedTerms: false });
    fixture.componentInstance.submit();

    expect(auth.register).not.toHaveBeenCalled();
  });

  it('rejects weak or mismatching passwords', () => {
    fixture.componentInstance.form.setValue({ ...valid, password: 'abc', confirmPassword: 'abc' });
    fixture.componentInstance.submit();
    expect(auth.register).not.toHaveBeenCalled();

    fixture.componentInstance.form.setValue({ ...valid, confirmPassword: 'Outra1234' });
    fixture.componentInstance.submit();
    expect(auth.register).not.toHaveBeenCalled();
  });

  it('registers with only the necessary data', () => {
    auth.register.and.returnValue(of(authResponse()));
    fixture.componentInstance.form.setValue(valid);

    fixture.componentInstance.submit();

    expect(auth.register).toHaveBeenCalledWith({
      name: 'Marina Alves',
      email: 'marina@teste.com',
      password: 'Senha1234',
      acceptedTerms: true
    });
  });
});
