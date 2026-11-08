package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.PublishTermsRequest;
import br.com.bussolaacademica.dto.TermsVersionResponse;
import br.com.bussolaacademica.exception.DuplicateResourceException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.TermsVersion;
import br.com.bussolaacademica.repository.TermsVersionRepository;
import br.com.bussolaacademica.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Versões do Termo de Uso e da Política de Privacidade.
 *
 * A versão vigente é consultada em toda requisição autenticada (filtro do novo aceite), por isso fica
 * guardada em memória e só é relida do banco quando o administrador publica uma versão nova.
 */
@Service
public class TermsService {

    private final TermsVersionRepository repository;
    private final AuditService auditService;
    private final CurrentUser currentUser;
    private final Clock clock;
    private final AtomicReference<TermsVersion> current = new AtomicReference<>();

    public TermsService(TermsVersionRepository repository, AuditService auditService, CurrentUser currentUser,
                        Clock clock) {
        this.repository = repository;
        this.auditService = auditService;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    public TermsVersion current() {
        TermsVersion cached = current.get();
        if (cached == null) {
            cached = repository.findFirstByOrderByPublishedAtDescIdDesc()
                    .orElseThrow(() -> new IllegalStateException("Nenhuma versão do Termo de Uso foi publicada"));
            current.set(cached);
        }
        return cached;
    }

    public String currentVersion() {
        return current().getVersion();
    }

    public boolean isCurrent(String version) {
        return currentVersion().equals(version);
    }

    public TermsVersionResponse currentResponse() {
        return TermsVersionResponse.from(current());
    }

    @Transactional(readOnly = true)
    public List<TermsVersionResponse> history() {
        return repository.findAllByOrderByPublishedAtDescIdDesc().stream().map(TermsVersionResponse::from).toList();
    }

    /** Publica uma versão nova: a partir daqui, todo usuário precisa aceitá-la no próximo acesso. */
    @Transactional
    public TermsVersionResponse publish(PublishTermsRequest request) {
        String version = request.version().trim();
        if (repository.existsByVersionIgnoreCase(version)) {
            throw new DuplicateResourceException("A versão " + version + " já foi publicada");
        }
        TermsVersion saved = repository.save(new TermsVersion(version, request.summary().trim(),
                request.termsOfUse().strip(), request.privacyPolicy().strip(),
                LocalDateTime.now(clock), currentUser.emailIfAuthenticated().orElse(null)));
        current.set(saved);
        auditService.recordForCurrentUser(AuditAction.TERMS_PUBLISHED,
                "Versão " + version + " publicada: " + saved.getSummary(), true);
        return TermsVersionResponse.from(saved);
    }

    /** Descarta a versão em memória (usado nos testes, que alteram o banco diretamente). */
    public void reload() {
        current.set(null);
    }
}
