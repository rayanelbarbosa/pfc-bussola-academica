package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    long countByUserEmailAndActionAndCreatedAtAfter(String userEmail, AuditAction action, LocalDateTime since);

    Optional<AuditLog> findFirstByUserEmailAndActionOrderByCreatedAtDesc(String userEmail, AuditAction action);

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Expurgo: apaga em uma única instrução os registros mais antigos que o limite. */
    @Modifying
    @Query("delete from AuditLog a where a.createdAt < :limit")
    int deleteOlderThan(@Param("limit") LocalDateTime limit);
}
