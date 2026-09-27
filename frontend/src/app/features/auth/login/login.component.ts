import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';

/** Tela de login (protótipo 01 - Login), apenas com e-mail e senha. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  /** Rota que o usuário tentou acessar antes de ser mandado para o login (?voltar=...). */
  readonly voltar = input<string>();
  /** Presente quando a sessão expirou (?expirou=1). */
  readonly expirou = input<string>();

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => this.router.navigateByUrl(this.safeReturnUrl()),
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível entrar. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  isInvalid(field: 'email' | 'password'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  /** Só aceita caminhos internos, para não redirecionar para sites externos. */
  private safeReturnUrl(): string {
    const url = this.voltar();
    return url && url.startsWith('/') && !url.startsWith('//') ? url : '/';
  }
}
