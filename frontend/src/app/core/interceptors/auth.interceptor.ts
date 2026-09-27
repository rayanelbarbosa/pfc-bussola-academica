import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Anexa o JWT (Authorization: Bearer ...) nas chamadas à API.
 * Se a API responder 401 com um token enviado, a sessão expirou: faz logout.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const isApiCall = req.url.startsWith(environment.apiUrl);
  const request = token && isApiCall ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse && error.status === 401 && token && isApiCall) {
        auth.logout(true);
      }
      return throwError(() => error);
    })
  );
};
