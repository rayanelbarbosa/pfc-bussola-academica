import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';

import { TermsVersion } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { getErrorMessage } from '../../../core/services/api-error';
import { LegalDocumentComponent } from '../../../shared/components/legal-document/legal-document.component';

/**
 * Política de Privacidade: texto da versão vigente, lido da API (o administrador altera pela tela "Termos e política").
 * Acessível a qualquer momento, com ou sem login.
 */
@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [DatePipe, LegalDocumentComponent],
  templateUrl: './privacy-policy.component.html'
})
export class PrivacyPolicyComponent implements OnInit {
  private readonly auth = inject(AuthService);

  readonly terms = signal<TermsVersion | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.auth.currentTerms().subscribe({
      next: (terms) => this.terms.set(terms),
      error: (err) => this.error.set(getErrorMessage(err, 'Não foi possível carregar o documento. Tente novamente.'))
    });
  }
}
