import { Component, OnInit, inject, input, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { PASSWORD_PATTERN } from '../register/register.component';

function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmation = group.get('confirmPassword')?.value;
  return password && confirmation && password !== confirmation ? { passwordsMismatch: true } : null;
}

/**
 * Criar nova senha (RF04) a partir do link recebido por e-mail (?token=...).
 * O link é conferido assim que a tela abre: se já foi usado ou expirou, o formulário nem aparece.
 * Depois de trocar, a pessoa entra de novo: as sessões abertas antes da troca deixam de valer.
 */
@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './reset-password.component.html'
})
export class ResetPasswordComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly token = input<string>();

  /** checking: conferindo o link · valid: pode criar a senha · invalid: link sem token, usado ou expirado. */
  readonly linkState = signal<'checking' | 'valid' | 'invalid'>('checking');
  readonly linkError = signal<string | null>(null);
  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group(
    {
      password: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
      confirmPassword: ['', Validators.required]
    },
    { validators: passwordsMatch }
  );

  ngOnInit(): void {
    const token = this.token();
    if (!token) {
      this.linkError.set('Link incompleto. Abra o link exatamente como chegou no e-mail.');
      this.linkState.set('invalid');
      return;
    }
    this.auth.validateResetToken(token).subscribe({
      next: () => this.linkState.set('valid'),
      error: (err) => {
        this.linkError.set(getErrorMessage(err, 'Este link de redefinição é inválido ou expirou.'));
        this.linkState.set('invalid');
      }
    });
  }

  submit(): void {
    const token = this.token();
    if (!token || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.resetPassword(token, this.form.getRawValue().password).subscribe({
      next: () => {
        this.auth.clear();
        this.router.navigate(['/login'], { queryParams: { senhaRedefinida: 1 } });
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível alterar a senha. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  isInvalid(field: 'password' | 'confirmPassword'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }

  get passwordsMismatch(): boolean {
    return this.form.hasError('passwordsMismatch') && this.form.controls.confirmPassword.touched;
  }
}
