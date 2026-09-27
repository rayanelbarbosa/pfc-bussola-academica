package br.com.bussolaacademica.model;

/** Perfis de acesso do sistema. */
public enum Role {
    /** Estudante: faz o teste vocacional e consulta apenas os próprios dados. */
    STUDENT,
    /** Administrador: além do acesso de estudante, consulta os logs de auditoria. */
    ADMIN
}
