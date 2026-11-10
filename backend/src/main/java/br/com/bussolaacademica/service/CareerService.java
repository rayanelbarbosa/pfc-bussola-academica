package br.com.bussolaacademica.service;

import br.com.bussolaacademica.dto.CareerRequest;
import br.com.bussolaacademica.dto.CareerResponse;
import br.com.bussolaacademica.dto.OptionResponse;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.exception.BusinessRuleException;
import br.com.bussolaacademica.exception.DuplicateResourceException;
import br.com.bussolaacademica.exception.ResourceNotFoundException;
import br.com.bussolaacademica.model.AuditAction;
import br.com.bussolaacademica.model.Career;
import br.com.bussolaacademica.model.CareerContent;
import br.com.bussolaacademica.model.InterestArea;
import br.com.bussolaacademica.model.RiasecCategory;
import br.com.bussolaacademica.repository.CareerRepository;
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
 * Gestão das carreiras pelo administrador.
 * <ul>
 *   <li>Cada carreira tem uma área de interesse e um código RIASEC de 2 letras (tipo principal + secundário),
 *       que alimentam a recomendação (compatibilidade = 2 × principal + secundário).</li>
 *   <li>Os tipos precisam ser diferentes entre si e a área não pode ser "Ainda não sei". O 3º tipo é opcional e
 *       só desempata carreiras com a mesma compatibilidade.</li>
 *   <li>O nome é único dentro da área (sem diferenciar maiúsculas de minúsculas).</li>
 *   <li>Não há exclusão: a carreira é desativada e deixa de aparecer na recomendação e na busca de vídeos.</li>
 *   <li>Toda alteração é registrada na auditoria.</li>
 * </ul>
 */
@Service
public class CareerService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final String DUPLICATE = "Já existe uma carreira com esse nome nessa área";

    private final CareerRepository repository;
    private final AuditService auditService;
    private final Clock clock;

    public CareerService(CareerRepository repository, AuditService auditService, Clock clock) {
        this.repository = repository;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PageResponse<CareerResponse> search(String name, InterestArea area, Boolean active, int page, int size) {
        Specification<Career> filter = Specification.where(null);
        if (name != null && !name.isBlank()) {
            String pattern = "%" + name.trim().toLowerCase() + "%";
            filter = filter.and((root, query, cb) -> cb.like(cb.lower(root.get("name")), pattern));
        }
        if (area != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("area"), area));
        }
        if (active != null) {
            filter = filter.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("area", "name"));
        return PageResponse.from(repository.findAll(filter, pageRequest).map(CareerResponse::from));
    }

    @Transactional(readOnly = true)
    public CareerResponse findById(Integer id) {
        return CareerResponse.from(getOrThrow(id));
    }

    /** Áreas que uma carreira pode ter (todas menos "Ainda não sei"). */
    public List<OptionResponse> areas() {
        return Arrays.stream(InterestArea.values())
                .filter(area -> area != InterestArea.UNDECIDED)
                .map(area -> new OptionResponse(area.name(), area.getLabel()))
                .toList();
    }

    public List<OptionResponse> categories() {
        return Arrays.stream(RiasecCategory.values())
                .map(category -> new OptionResponse(category.name(), category.getLabel()))
                .toList();
    }

    @Transactional
    public CareerResponse create(CareerRequest request) {
        CareerContent content = validate(request);
        if (repository.existsByNameIgnoreCaseAndArea(content.name(), content.area())) {
            throw new DuplicateResourceException(DUPLICATE);
        }
        Career saved = repository.save(new Career(content, request.activeOrDefault(), LocalDateTime.now(clock)));
        auditService.recordForCurrentUser(AuditAction.CAREER_CREATED,
                "Incluiu a carreira \"" + saved.getName() + "\" (" + saved.getCode() + ")", true);
        return CareerResponse.from(saved);
    }

    @Transactional
    public CareerResponse update(Integer id, CareerRequest request) {
        Career career = getOrThrow(id);
        CareerContent content = validate(request);
        if (repository.existsByNameIgnoreCaseAndAreaAndIdNot(content.name(), content.area(), id)) {
            throw new DuplicateResourceException(DUPLICATE);
        }
        career.update(content, request.activeOrDefault(), LocalDateTime.now(clock));
        auditService.recordForCurrentUser(AuditAction.CAREER_UPDATED,
                "Alterou a carreira \"" + career.getName() + "\" (" + career.getCode() + ")", true);
        return CareerResponse.from(career);
    }

    @Transactional
    public CareerResponse changeStatus(Integer id, boolean active) {
        Career career = getOrThrow(id);
        career.changeStatus(active, LocalDateTime.now(clock));
        auditService.recordForCurrentUser(AuditAction.CAREER_STATUS_CHANGED,
                (active ? "Reativou" : "Desativou") + " a carreira \"" + career.getName() + "\"", true);
        return CareerResponse.from(career);
    }

    private CareerContent validate(CareerRequest request) {
        CareerContent content = request.toContent();
        if (content.area() == InterestArea.UNDECIDED) {
            throw new BusinessRuleException("Escolha uma área de interesse para a carreira");
        }
        if (content.primaryCategory() == content.secondaryCategory()) {
            throw new BusinessRuleException("O tipo principal e o secundário precisam ser diferentes");
        }
        if (content.tertiaryCategory() != null && (content.tertiaryCategory() == content.primaryCategory()
                || content.tertiaryCategory() == content.secondaryCategory())) {
            throw new BusinessRuleException("O 3º tipo precisa ser diferente do principal e do secundário");
        }
        return content;
    }

    private Career getOrThrow(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Carreira não encontrada"));
    }
}
