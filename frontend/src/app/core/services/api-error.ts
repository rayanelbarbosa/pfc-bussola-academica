import { HttpErrorResponse } from '@angular/common/http';

/** Converte um erro HTTP em mensagem amigável, usando o formato de erro da API quando existir. */
export function getErrorMessage(error: unknown, fallback: string): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.';
    }
    const message = error.error?.message;
    if (typeof message === 'string' && message.length > 0) {
      return message;
    }
  }
  return fallback;
}
