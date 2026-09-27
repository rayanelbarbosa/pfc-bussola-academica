package br.com.bussolaacademica.model;

/** Ações registradas no log de auditoria. */
public enum AuditAction {
    USER_REGISTERED,
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_DELETED,
    PERSONAL_DATA_VIEWED,
    TEST_SUBMITTED,
    RESULT_VIEWED,
    VIDEOS_SEARCHED,
    AUDIT_LOG_VIEWED,
    ACCESS_DENIED
}
