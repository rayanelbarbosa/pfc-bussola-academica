package br.com.bussolaacademica.model;

/**
 * Ações registradas no log de auditoria: segurança, alteração de dados e ações administrativas.
 * Consultas rotineiras não são mais registradas; os valores marcados como "antigo" ficam aqui
 * só para os registros gravados antes dessa mudança continuarem legíveis.
 */
public enum AuditAction {
    USER_REGISTERED,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGIN_BLOCKED,
    LOGOUT,
    ACCOUNT_DELETED,
    PERSONAL_DATA_EXPORTED,
    TERMS_ACCEPTED,
    TERMS_PUBLISHED,
    TEST_SUBMITTED,
    ACCESS_DENIED,
    ADMISSION_METHOD_CREATED,
    ADMISSION_METHOD_UPDATED,
    ADMISSION_METHOD_STATUS_CHANGED,
    ADMIN_CREATED,
    USER_ROLE_CHANGED,
    AUDIT_LOGS_PURGED,
    /** antigo: consulta do histórico de resultados */
    PERSONAL_DATA_VIEWED,
    /** antigo: consulta de um resultado */
    RESULT_VIEWED,
    /** antigo: busca de vídeos */
    VIDEOS_SEARCHED,
    /** antigo: consulta da própria auditoria */
    AUDIT_LOG_VIEWED
}
