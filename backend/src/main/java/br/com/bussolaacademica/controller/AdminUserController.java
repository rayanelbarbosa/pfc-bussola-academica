package br.com.bussolaacademica.controller;

import br.com.bussolaacademica.dto.AdminUserResponse;
import br.com.bussolaacademica.dto.CreateAdminRequest;
import br.com.bussolaacademica.dto.PageResponse;
import br.com.bussolaacademica.dto.RoleChangeRequest;
import br.com.bussolaacademica.model.Role;
import br.com.bussolaacademica.service.AdminUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Gestão de usuários e administradores (RF12): listar, cadastrar admin, trocar perfil e excluir conta. Só ADMIN. */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService service;

    public AdminUserController(AdminUserService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<AdminUserResponse> search(@RequestParam(required = false) String search,
                                                  @RequestParam(required = false) Role role,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return service.search(search, role, page, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserResponse createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        return service.createAdmin(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        service.deleteUser(id);
    }

    @PatchMapping("/{id}/role")
    public AdminUserResponse changeRole(@PathVariable Long id, @Valid @RequestBody RoleChangeRequest request) {
        return service.changeRole(id, request.role());
    }
}
