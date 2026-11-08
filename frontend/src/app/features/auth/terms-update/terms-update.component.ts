import { Component, OnInit, inject, input, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { TermsVersion } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { IconComponent } from '../../../shared/components/icon/icon.component';

/**
 * Novo aceite (RF02): quando o administrador publica uma versão nova do Termo de Uso e da Política de
 * Privacidade, o usuário vê o resumo da mudança e precisa aceitar para continuar usando a plataforma.
 */
@Component({
  selector: 'app-terms-update',
  standalone: true,
  imports: [DatePipe, FormsModule, RouterLink, IconComponent],
  templateUrl: './terms-update.component.html'
})
export class TermsUpdateComponent implements OnInit {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Rota para onde voltar depois do aceite (?voltar=...). */
  readonly voltar = input<string>();

  readonly terms = signal<TermsVersion | null>(null);
  readonly error = signal<string | null>(null);
  readonly submitting = signal(false);
  accepted = false;

  ngOnInit(): void {
    const pending = this.auth.pendingTerms();
    if (pending) {
      this.terms.set(pending);
      return;
    }
    this.auth.currentTerms().subscribe({
      next: (terms) => this.terms.set(terms),
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível carregar os termos.'))
    });
  }

  accept(): void {
    const terms = this.terms();
    if (!terms || !this.accepted) {
      return;
    }
    this.submitting.set(true);
    this.error.set(null);
    this.auth.acceptTerms(terms.version).subscribe({
      next: () => this.router.navigateByUrl(this.safeReturnUrl()),
      error: (err) => {
        this.error.set(getErrorMessage(err, 'Não foi possível registrar o aceite. Tente novamente.'));
        this.submitting.set(false);
      }
    });
  }

  logout(): void {
    this.auth.logout();
  }

  private safeReturnUrl(): string {
    const url = this.voltar();
    return url && url.startsWith('/') && !url.startsWith('//') && !url.startsWith('/novo-aceite') ? url : '/';
  }
}
