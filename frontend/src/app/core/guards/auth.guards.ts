import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** Só usuários logados. Quem não está logado vai para o login e volta depois. */
export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  return auth.token() && auth.isLoggedIn()
    ? true
    : inject(Router).createUrlTree(['/login'], { queryParams: { voltar: state.url } });
};

/** Só administradores. */
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  if (!auth.token() || !auth.isLoggedIn()) {
    return inject(Router).createUrlTree(['/login']);
  }
  return auth.isAdmin() ? true : inject(Router).createUrlTree(['/']);
};

/** Telas de login/cadastro: quem já está logado vai direto para a Home. */
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.token() && auth.isLoggedIn() ? inject(Router).createUrlTree(['/']) : true;
};
