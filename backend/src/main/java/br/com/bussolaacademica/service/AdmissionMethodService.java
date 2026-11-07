package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.AdmissionMethodRequest;
import br.com.bussolaacademica.dto.AdmissionMethodResponse;
import br.com.bussolaacademica.dto.OptionResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.exception.DuplicateResourceException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AdmissionMethod;
import br.com.bussolaacademica.model.AdmissionMethodContent;
import br.com.bussolaacademica.model.AdmissionType;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.repository.AdmissionMethodRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Regras das formas de ingresso (RF01 e RF11).
 * <ul>
 *   <li>A página pública mostra só as formas ativas, na ordem de exibição.</li>
 *   <li>O nome é único (sem diferenciar maiúsculas de minúsculas).</li>
 *   <li>Não há exclusão: o administrador desativa a forma de ingresso.</li>
 *   <li>Toda alteração atualiza a data da última atualização e é registrada na auditoria.</li>
 * </ul>
 */
@Service
public class AdmissionMethodService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AdmissionMethodRepository repository;
    private final AuditService auditService;
    private final Clock clock;

    public AdmissionMethodService(AdmissionMethodRepository repository, AuditService auditService, Clock clock) {
        this.repository = repository;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AdmissionMethodResponse> listActive(AdmissionType type) {
        return repository.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .filter(method -> type == null || method.getType() == type)
                .map(AdmissionMethodResponse::from)
                .toList();
    }

    public List<OptionResponse> types() {
        return Arrays.stream(AdmissionType.values())
                .map(type -> new OptionResponse(type.name(), type.getLabel()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AdmissionMethodResponse> search(String name, AdmissionType type, Boolean active,
                                                        int page, int size) {
        Specification<AdmissionMethod> filter = Specification.where(null);
        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            filter = filter.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        if (type != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("type"), type));
        }
        if (active != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("displayOrder", "name"));
        return PageResponse.from(repository.findAll(filter, pageRequest).map(AdmissionMethodResponse::from));
    }

    @Transactional(readOnly = true)
    public AdmissionMethodResponse findById(Long id) {
        return AdmissionMethodResponse.from(getOrThrow(id));
    }

    @Transactional
    public AdmissionMethodResponse create(AdmissionMethodRequest request) {
        AdmissionMethodContent content = request.toContent();
        if (repository.existsByNameIgnoreCase(content.name())) {
            throw new DuplicateResourceException("Já existe uma forma de ingresso com esse nome");
        }
        AdmissionMethod method = new AdmissionMethod(content, request.activeOrDefault(),
                repository.findMaxDisplayOrder() + 1, LocalDateTime.now(clock));
        AdmissionMethod saved = repository.save(method);
        auditService.recordForCurrentUser(AuditAction.ADMISSION_METHOD_CREATED,
                "Incluiu a forma de ingresso \"" + saved.getName() + "\"", true);
        return AdmissionMethodResponse.from(saved);
    }

    @Transactional
    public AdmissionMethodResponse update(Long id, AdmissionMethodRequest request) {
        AdmissionMethod method = getOrThrow(id);
        AdmissionMethodContent content = request.toContent();
        if (repository.existsByNameIgnoreCaseAndIdNot(content.name(), id)) {
            throw new DuplicateResourceException("Já existe uma forma de ingresso com esse nome");
        }
        method.update(content, request.activeOrDefault(), LocalDateTime.now(clock));
        auditService.recordForCurrentUser(AuditAction.ADMISSION_METHOD_UPDATED,
                "Alterou a forma de ingresso \"" + method.getName() + "\"", true);
        return AdmissionMethodResponse.from(method);
    }

    @Transactional
    public AdmissionMethodResponse changeStatus(Long id, boolean active) {
        AdmissionMethod method = getOrThrow(id);
        method.changeStatus(active, LocalDateTime.now(clock));
        auditService.recordForCurrentUser(AuditAction.ADMISSION_METHOD_STATUS_CHANGED,
                (active ? "Reativou" : "Desativou") + " a forma de ingresso \"" + method.getName() + "\"", true);
        return AdmissionMethodResponse.from(method);
    }

    private AdmissionMethod getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forma de ingresso não encontrada"));
    }
}
