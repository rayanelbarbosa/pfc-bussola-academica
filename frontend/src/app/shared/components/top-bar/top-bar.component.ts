import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

/** Barra superior (protótipo): marca, navegação conforme o perfil, nome do usuário e Sair. */
@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="top-bar">
      <a routerLink="/" class="brand">Bússola Acadêmica</a>
      @if (auth.user(); as user) {
        <nav class="user-area" aria-label="Menu do usuário">
          <a routerLink="/meus-dados" routerLinkActive="active">Meus dados</a>
          @if (auth.isAdmin()) {
            <a routerLink="/admin/auditoria" routerLinkActive="active">Auditoria</a>
          }
          <span class="user-name">{{ user.name }}</span>
          <button type="button" class="logout" (click)="auth.logout()">↩ Sair</button>
        </nav>
      }
    </header>
  `,
  styles: `
    .top-bar {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
    }
    .brand {
      font-size: 20px;
      font-weight: 700;
      color: var(--color-primary);
      text-decoration: none;
    }
    .user-area {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 20px;
      font-size: 14px;
    }
    .user-area a {
      color: var(--color-text-muted);
      font-weight: 500;
      text-decoration: none;
    }
    .user-area a.active,
    .user-area a:hover {
      color: var(--color-primary);
    }
    .user-name {
      font-weight: 500;
      color: var(--color-text);
    }
    .logout {
      padding: 0;
      border: none;
      background: none;
      color: var(--color-text-muted);
      font-weight: 500;
      cursor: pointer;
    }
  `
})
export class TopBarComponent {
  readonly auth = inject(AuthService);
}
