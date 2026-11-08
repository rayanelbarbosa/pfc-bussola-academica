import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/**
 * Esqueci minha senha (RF04): pede o link por e-mail. A mensagem de confirmação é sempre a mesma,
 * para não revelar se o e-mail tem conta.
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, IconComponent],
  templateUrl: './forgot-password.component.html'
})
export class ForgotPasswordComponent {
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly submitting = signal(false);
  readonly error = signal<string | null>(null);
  readonly sent = signal<string | null>(null);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email, Validators.maxLength(150)]]
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.forgotPassword(this.form.getRawValue().email.trim()).subscribe({
      next: (res) => {
        this.sent.set(res.message);
        this.submitting.set(false);
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível enviar o pedido. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  isInvalid(): boolean {
    const control = this.form.controls.email;
    return control.invalid && control.touched;
  }
}
