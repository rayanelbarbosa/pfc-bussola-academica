import { HttpErrorResponse } from '@angular/common/http';
import { getErrorMessage } from './api-error';

describe('getErrorMessage', () => {
  it('uses the message sent by the API', () => {
    const error = new HttpErrorResponse({ status: 400, error: { message: 'Responda todas as perguntas.' } });
    expect(getErrorMessage(error, 'fallback')).toBe('Responda todas as perguntas.');
  });

  it('warns when the server is unreachable', () => {
    expect(getErrorMessage(new HttpErrorResponse({ status: 0 }), 'fallback')).toContain('conectar ao servidor');
  });

  it('uses the fallback message otherwise', () => {
    expect(getErrorMessage(new Error('x'), 'fallback')).toBe('fallback');
  });
});
