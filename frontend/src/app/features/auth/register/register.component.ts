import { Component, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { LEGAL_VERSION } from '../../../core/models/auth.model';

/** Senha com pelo menos 8 caracteres, letras e números (mesma regra do back-end). */
export const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,72}$/;

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmation = group.get('confirmPassword')?.value;
  return password && confirmation && password !== confirmation ? { passwordsMismatch: true } : null;
}

/** Cadastro de estudante: só os dados necessários + aceite obrigatório do Termo e da Política (LGPD). */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly legalVersion = LEGAL_VERSION;
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      name: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]],
      password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', Validators.required],
      acceptedTerms: [false, Validators.requiredTrue]
    },
    { validators: passwordsMatch }
  );

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { name, email, password, acceptedTerms } = this.form.getRawValue();
    this.submitting.set(true);
    this.error.set(null);
    this.auth.register({ name: name.trim(), email: email.trim(), password, acceptedTerms }).subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível criar sua conta. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  isInvalid(field: 'name' | 'email' | 'password' | 'confirmPassword' | 'acceptedTerms'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  get passwordsMismatch(): boolean {
    return this.form.hasError('passwordsMismatch') && this.form.controls.confirmPassword.touched;
  }
}
