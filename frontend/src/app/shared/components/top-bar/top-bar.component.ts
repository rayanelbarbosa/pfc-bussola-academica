import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

/** Barra superior com a marca. A área do usuário (nome + Sair) entra junto com o login. */
@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [RouterLink],
  template: `
    <header class="top-bar">
      <a routerLink="/" class="brand">Bússola Acadêmica</a>
      <div class="user-area">
        <ng-content />
      </div>
    </header>
  `,
  styles: `
    .top-bar {
      display: flex;
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
      align-items: center;
      gap: 20px;
      font-size: 14px;
    }
  `
})
export class TopBarComponent {}
