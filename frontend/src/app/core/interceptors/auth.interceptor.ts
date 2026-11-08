import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Anexa o JWT (Authorization: Bearer ...) nas chamadas à API.
 * Se a API responder 401 com um token enviado, a sessão expirou: faz logout.
 * Se responder 428, falta aceitar a versão nova dos termos: vai para a tela de novo aceite.
 */
/** 428: o administrador publicou uma versão nova dos termos e o usuário ainda não aceitou. */
const TERMS_PENDING = 428;

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const token = auth.token();
  const isApiCall = req.url.startsWith(environment.apiUrl);
  const request = token && isApiCall ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (!(error instanceof HttpErrorResponse) || !token || !isApiCall) {
        return throwError(() => error);
      }
      if (error.status === 401 && !req.url.endsWith('/auth/logout')) {
        auth.logout(true);
      } else if (error.status === TERMS_PENDING) {
        router.navigate(['/novo-aceite']);
      }
      return throwError(() => error);
    })
  );
};
