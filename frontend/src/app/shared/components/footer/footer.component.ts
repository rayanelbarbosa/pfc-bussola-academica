import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Rodapé presente em todas as telas: Termo de Uso e Política de Privacidade sempre acessíveis (LGPD). */
@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [RouterLink],
  template: `
    <footer class="footer">
      <span>© 2026 Bússola Acadêmica · Projeto Final de Curso — UMC</span>
      <nav aria-label="Documentos legais">
        <a routerLink="/termos-de-uso">Termo de Uso</a>
        <a routerLink="/politica-de-privacidade">Política de Privacidade</a>
      </nav>
    </footer>
  `,
  styles: `
    .footer {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      padding: 20px var(--page-padding);
      border-top: 1px solid var(--color-border);
      background: var(--color-surface);
      font-size: 13px;
      color: var(--color-text-muted);
    }
    nav {
      display: flex;
      gap: 20px;
    }
    a {
      color: var(--color-primary);
      font-weight: 500;
      text-decoration: none;
    }
    a:hover {
      text-decoration: underline;
    }
  `
})
export class FooterComponent {}
