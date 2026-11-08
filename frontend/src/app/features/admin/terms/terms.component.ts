import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { TermsVersion } from '../../../core/models/auth.model';
import { TermsAdminService } from '../../../core/services/terms-admin.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';
import { LegalDocumentComponent } from '../../../shared/components/legal-document/legal-document.component';

type Tab = 'termsOfUse' | 'privacyPolicy';

/**
 * Termo de Uso e Política de Privacidade (área do administrador): o formulário vem com o texto vigente,
 * o administrador edita, confere na pré-visualização e publica como uma versão nova, com o resumo da mudança.
 * Versões publicadas não mudam; todos os usuários (inclusive administradores) precisam aceitar a nova.
 */
@Component({
  selector: 'app-admin-terms',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, IconComponent, LegalDocumentComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './terms.component.html'
})
export class TermsComponent implements OnInit {
  private readonly service = inject(TermsAdminService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  readonly versions = signal<TermsVersion[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  readonly confirming = signal(false);
  readonly publishing = signal(false);
  readonly tab = signal<Tab>('termsOfUse');
  readonly preview = signal(false);
  readonly viewing = signal<TermsVersion | null>(null);

  readonly form = this.fb.nonNullable.group({
    version: ['', [Validators.required, Validators.maxLength(20), Validators.pattern(/^\d+(\.\d+){0,2}$/)]],
    summary: ['', [Validators.required, Validators.maxLength(500)]],
    termsOfUse: ['', [Validators.required, Validators.maxLength(20000)]],
    privacyPolicy: ['', [Validators.required, Validators.maxLength(20000)]]
  });

  ngOnInit(): void {
    this.service.history().subscribe({
      next: (versions) => {
        this.versions.set(versions);
        this.loading.set(false);
        const current = versions[0];
        if (current) {
          this.form.patchValue({
            version: nextVersion(current.version),
            termsOfUse: current.termsOfUse,
            privacyPolicy: current.privacyPolicy
          });
        }
      },
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível carregar as versões.'));
        this.loading.set(false);
      }
    });
  }

  askConfirmation(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.confirming.set(true);
  }

  publish(): void {
    const value = this.form.getRawValue();
    this.publishing.set(true);
    this.error.set(null);
    this.service
      .publish({
        version: value.version.trim(),
        summary: value.summary.trim(),
        termsOfUse: value.termsOfUse,
        privacyPolicy: value.privacyPolicy
      })
      .subscribe({
        // A própria administradora também precisa aceitar a versão nova
        next: () => this.router.navigate(['/novo-aceite'], { queryParams: { voltar: '/admin/termos' } }),
        error: (err) => {
          this.error.set(getErrorMessage(err, 'Não foi possível publicar a nova versão.'));
          this.publishing.set(false);
          this.confirming.set(false);
        }
      });
  }

  isInvalid(field: 'version' | 'summary' | 'termsOfUse' | 'privacyPolicy'): boolean {
    const control = this.form.controls[field];
    return control.invalid && control.touched;
  }
}

/** Sugere a próxima versão: 1.0 vira 1.1, 2 vira 2.1. */
export function nextVersion(version: string): string {
  const parts = version.split('.').map((part) => Number(part));
  if (parts.some((part) => Number.isNaN(part))) {
    return '';
  }
  if (parts.length === 1) {
    return `${parts[0]}.1`;
  }
  parts[parts.length - 1] += 1;
  return parts.join('.');
}
