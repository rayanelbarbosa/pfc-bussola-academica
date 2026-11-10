import { Component, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { PasswordToggleComponent } from '../../../shared/components/password-toggle/password-toggle.component';

/** Tela de login (RF03): e-mail e senha; o perfil (estudante ou administrador) vem do token. */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, IconComponent, PasswordToggleComponent],
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
  /** Presente depois de redefinir a senha pelo link do e-mail (?senhaRedefinida=1). */
  readonly senhaRedefinida = input<string>();

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
      next: (res) =>
        res.pendingTerms
          ? this.router.navigate(['/novo-aceite'], { queryParams: { voltar: this.safeReturnUrl() } })
          : this.router.navigateByUrl(this.safeReturnUrl()),
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
