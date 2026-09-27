export interface AuditLog {
  id: number;
  createdAt: string;
  userId: number | null;
  userEmail: string | null;
  action: string;
  resource: string | null;
  details: string | null;
  ipAddress: string | null;
  success: boolean;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

/** Rótulos em português das ações auditadas. */
export const AUDIT_ACTION_LABELS: Record<string, string> = {
  USER_REGISTERED: 'Cadastro',
  LOGIN_SUCCESS: 'Login',
  LOGIN_FAILURE: 'Login recusado',
  ACCOUNT_DELETED: 'Conta excluída',
  PERSONAL_DATA_VIEWED: 'Consulta aos próprios dados',
  TEST_SUBMITTED: 'Teste respondido',
  RESULT_VIEWED: 'Resultado consultado',
  VIDEOS_SEARCHED: 'Busca de vídeos (YouTube)',
  AUDIT_LOG_VIEWED: 'Consulta à auditoria',
  ACCESS_DENIED: 'Acesso negado'
};
