package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AuditIntegrityResponse;
import br.com.bussolaacademica.model.AuditLog;
import br.com.bussolaacademica.repository.AuditChainHeadRepository;
import br.com.bussolaacademica.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Regra de negócio: proteção dos registros de auditoria contra alteração (encadeamento por hash).
 *
 * <p>Cada registro guarda o hash do anterior e o próprio hash (SHA-256 do hash anterior + conteúdo). A verificação
 * percorre os registros na ordem em que foram gravados e confere, para cada um:</p>
 * <ol>
 *   <li>o hash recalculado bate com o guardado (senão, o registro foi <b>alterado</b>);</li>
 *   <li>o hash anterior guardado é o hash do registro de antes (senão, um registro foi <b>apagado ou inserido</b>
 *       no meio);</li>
 *   <li>depois do início da corrente, nenhum registro está sem lacre (senão, foi <b>inserido</b> por fora);</li>
 *   <li>o último registro é o último da corrente guardado à parte (senão, o mais recente foi <b>apagado</b>).</li>
 * </ol>
 * <p>O primeiro registro lacrado que sobrou é o início da corrente: o expurgo de 6 meses apaga os mais antigos de
 * propósito, então o hash anterior dele não é conferido. Registros gravados antes da proteção ficam sem lacre e não
 * entram na corrente.</p>
 */
@Service
public class AuditIntegrityService {

    private final AuditLogRepository repository;
    private final AuditChainHeadRepository chainHeadRepository;
    private final Clock clock;

    public AuditIntegrityService(AuditLogRepository repository, AuditChainHeadRepository chainHeadRepository,
                                 Clock clock) {
        this.repository = repository;
        this.chainHeadRepository = chainHeadRepository;
        this.clock = clock;
    }

    /**
     * Confere a corrente inteira. Não é somente leitura porque trava o último hash (FOR SHARE) até terminar: assim
     * nenhum registro novo entra no meio da conferência.
     */
    @Transactional
    public AuditIntegrityResponse verify() {
        String expectedPrevious = null;
        boolean chainStarted = false;
        long checked = 0;
        try (Stream<AuditLog> logs = repository.streamAllByOrderByIdAsc()) {
            Iterator<AuditLog> iterator = logs.iterator();
            while (iterator.hasNext()) {
                AuditLog log = iterator.next();
                if (log.getHash() == null) {
                    if (chainStarted) {
                        return broken(checked, log, "O registro " + log.getId() + " foi inserido sem lacre.");
                    }
                    continue;
                }
                if (chainStarted && !Objects.equals(log.getPreviousHash(), expectedPrevious)) {
                    return broken(checked, log, "Um registro foi apagado ou inserido antes do registro " + log.getId() + ".");
                }
                if (!log.getHash().equals(log.computeHash())) {
                    return broken(checked, log, "O registro " + log.getId() + " foi alterado depois de gravado.");
                }
                chainStarted = true;
                expectedPrevious = log.getHash();
                checked++;
            }
        }
        if (!Objects.equals(chainHeadRepository.lockHeadForRead().getLastHash(), expectedPrevious)) {
            return new AuditIntegrityResponse(false, checked, null,
                    "O registro mais recente da corrente foi apagado.", LocalDateTime.now(clock));
        }
        return new AuditIntegrityResponse(true, checked, null,
                "Corrente íntegra: " + checked + " registros conferidos, nenhum alterado ou apagado.", LocalDateTime.now(clock));
    }

    private AuditIntegrityResponse broken(long checked, AuditLog log, String message) {
        return new AuditIntegrityResponse(false, checked, log.getId(), message, LocalDateTime.now(clock));
    }
}
