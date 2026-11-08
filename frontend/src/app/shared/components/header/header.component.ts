import { ChangeDetectionStrategy, Component, HostListener, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { CompassMarkComponent } from '../compass/compass.component';
import { IconComponent } from '../icon/icon.component';

/** Cabeçalho do site: links mudam conforme o perfil (visitante, estudante ou administrador). */
@Component({
  selector: 'app-header',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CompassMarkComponent, IconComponent],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './header.component.html'
})
export class HeaderComponent {
  private readonly auth = inject(AuthService);

  readonly user = this.auth.user;
  readonly isLoggedIn = this.auth.isLoggedIn;
  readonly isAdmin = this.auth.isAdmin;
  readonly firstName = computed(() => this.user()?.name.trim().split(/\s+/)[0] ?? 'Conta');
  readonly initial = computed(() => this.firstName().charAt(0).toUpperCase());

  readonly mobileOpen = signal(false);
  readonly openMenu = signal<'admin' | 'user' | null>(null);

  toggleMenu(menu: 'admin' | 'user', event: Event): void {
    event.stopPropagation();
    this.openMenu.update((current) => (current === menu ? null : menu));
  }

  @HostListener('document:click')
  @HostListener('document:keydown.escape')
  closeMenus(): void {
    this.openMenu.set(null);
  }

  logout(): void {
    this.mobileOpen.set(false);
    this.auth.logout();
  }
}
