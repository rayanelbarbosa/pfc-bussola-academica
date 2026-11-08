package br.com.bussolaacademica.repository;

import br.com.bussolaacademica.model.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface RevokedTokenRepository extends JpaRepository<RevokedToken, String> {

    /** Tokens já expirados não precisam mais ficar na lista: a validação de data já os recusa. */
    @Modifying
    @Query("delete from RevokedToken t where t.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
