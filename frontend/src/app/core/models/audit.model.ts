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
  LOGIN_BLOCKED: 'Login bloqueado (muitas tentativas)',
  LOGOUT: 'Saiu da conta',
  PASSWORD_RESET_REQUESTED: 'Pedido de nova senha',
  PASSWORD_RESET_COMPLETED: 'Senha redefinida por e-mail',
  ACCOUNT_DELETED: 'Conta excluída',
  PERSONAL_DATA_EXPORTED: 'Exportação dos dados pessoais',
  TERMS_ACCEPTED: 'Aceite dos termos',
  TERMS_PUBLISHED: 'Nova versão dos termos publicada',
  TEST_SUBMITTED: 'Teste respondido',
  ACCESS_DENIED: 'Acesso negado',
  ADMISSION_METHOD_CREATED: 'Forma de ingresso incluída',
  ADMISSION_METHOD_UPDATED: 'Forma de ingresso alterada',
  ADMISSION_METHOD_STATUS_CHANGED: 'Forma de ingresso ativada/desativada',
  ADMIN_CREATED: 'Administrador cadastrado',
  USER_ROLE_CHANGED: 'Perfil de usuário alterado',
  AUDIT_LOGS_PURGED: 'Expurgo de registros antigos',
  PERSONAL_DATA_VIEWED: 'Consulta aos próprios dados (antigo)',
  RESULT_VIEWED: 'Resultado consultado (antigo)',
  VIDEOS_SEARCHED: 'Busca de vídeos (antigo)',
  AUDIT_LOG_VIEWED: 'Consulta à auditoria (antigo)'
};
