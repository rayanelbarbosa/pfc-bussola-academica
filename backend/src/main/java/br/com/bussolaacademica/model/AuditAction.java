package br.com.bussolaacademica.model;

/**
 * Ações registradas no log de auditoria: segurança, alteração de dados e ações administrativas.
 * Consultas rotineiras não são mais registradas; os valores marcados como "antigo" ficam aqui
 * só para os registros gravados antes dessa mudança continuarem legíveis.
 * O rótulo em português é usado na exportação dos dados pessoais (LGPD), lida pelo próprio titular.
 */
public enum AuditAction {
    USER_REGISTERED("Cadastro"),
    LOGIN_SUCCESS("Login"),
    LOGIN_FAILURE("Login recusado"),
    LOGIN_BLOCKED("Login bloqueado (muitas tentativas)"),
    LOGOUT("Saiu da conta"),
    PASSWORD_RESET_REQUESTED("Pedido de nova senha"),
    PASSWORD_RESET_COMPLETED("Senha redefinida por e-mail"),
    ACCOUNT_DELETED("Conta excluída"),
    PERSONAL_DATA_EXPORTED("Exportação dos dados pessoais"),
    TERMS_ACCEPTED("Aceite dos termos"),
    TERMS_PUBLISHED("Nova versão dos termos publicada"),
    TEST_SUBMITTED("Teste respondido"),
    ACCESS_DENIED("Acesso negado"),
    ADMISSION_METHOD_CREATED("Forma de ingresso incluída"),
    ADMISSION_METHOD_UPDATED("Forma de ingresso alterada"),
    ADMISSION_METHOD_STATUS_CHANGED("Forma de ingresso ativada/desativada"),
    ADMIN_CREATED("Administrador cadastrado"),
    USER_ROLE_CHANGED("Perfil de usuário alterado"),
    AUDIT_LOGS_PURGED("Expurgo de registros antigos"),
    /** antigo: consulta do histórico de resultados */
    PERSONAL_DATA_VIEWED("Consulta aos próprios dados"),
    /** antigo: consulta de um resultado */
    RESULT_VIEWED("Resultado consultado"),
    /** antigo: busca de vídeos */
    VIDEOS_SEARCHED("Busca de vídeos"),
    /** antigo: consulta da própria auditoria */
    AUDIT_LOG_VIEWED("Consulta à auditoria");

    private final String label;

    AuditAction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
