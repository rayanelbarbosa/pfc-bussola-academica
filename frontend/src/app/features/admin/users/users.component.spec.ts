import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { AdminUser, AdminUserService } from '../../../core/services/admin-user.service';
import { authResponse } from '../../../core/testing/auth-fixtures';
import { UsersComponent } from './users.component';

const ME: AdminUser = { id: 1, name: 'Marina Alves', email: 'marina@teste.com', role: 'ADMIN', createdAt: '2026-09-27T10:00:00' };
const STUDENT: AdminUser = { id: 2, name: 'João', email: 'joao@teste.com', role: 'STUDENT', createdAt: '2026-11-01T10:00:00' };

describe('UsersComponent', () => {
  let fixture: ComponentFixture<UsersComponent>;
  let service: jasmine.SpyObj<AdminUserService>;

  beforeEach(async () => {
    const admin = authResponse('ADMIN');
    localStorage.setItem('bussola.token', admin.token);
    localStorage.setItem('bussola.user', JSON.stringify(admin.user));
    service = jasmine.createSpyObj('AdminUserService', ['search', 'createAdmin', 'changeRole', 'deleteUser']);
    service.search.and.returnValue(of({ content: [ME, STUDENT], page: 0, size: 20, totalElements: 2, totalPages: 1 }));
    await TestBed.configureTestingModule({
      imports: [UsersComponent],
      providers: [provideRouter([]), provideHttpClient(), { provide: AdminUserService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(UsersComponent);
    fixture.detectChanges();
  });

  afterEach(() => localStorage.clear());

  it('blocks changing your own role and offers it for the others', () => {
    const rows = fixture.nativeElement.querySelectorAll('tbody tr');
    expect(rows[0].textContent).toContain('Sua conta: use Meus dados');
    expect(rows[0].textContent).not.toContain('Excluir');
    expect(rows[1].textContent).toContain('Tornar administrador');
    expect(rows[1].textContent).toContain('Excluir');
  });

  it('promotes after confirmation', () => {
    service.changeRole.and.returnValue(of({ ...STUDENT, role: 'ADMIN' }));
    fixture.componentInstance.confirming.set(STUDENT);
    fixture.componentInstance.confirmRoleChange();

    expect(service.changeRole).toHaveBeenCalledWith(2, 'ADMIN');
    expect(fixture.componentInstance.notice()).toContain('administrador');
  });

  it('deletes another account after confirmation', () => {
    service.deleteUser.and.returnValue(of(undefined));
    fixture.componentInstance.deleting.set(STUDENT);
    fixture.componentInstance.confirmDelete();

    expect(service.deleteUser).toHaveBeenCalledWith(2);
    expect(fixture.componentInstance.notice()).toContain('joao@teste.com');
  });

  it('validates the new admin form before sending', () => {
    const component = fixture.componentInstance;
    component.openCreate();
    component.form.setValue({ name: 'Nova', email: 'nova@teste.com', password: 'fraca' });
    component.saveAdmin();
    expect(service.createAdmin).not.toHaveBeenCalled();

    service.createAdmin.and.returnValue(of({ ...STUDENT, id: 3, email: 'nova@teste.com', role: 'ADMIN' }));
    component.form.setValue({ name: 'Nova', email: 'nova@teste.com', password: 'Admin2026x' });
    component.saveAdmin();
    expect(service.createAdmin).toHaveBeenCalledWith({ name: 'Nova', email: 'nova@teste.com', password: 'Admin2026x' });
  });

  const fail = () => throwError(() => new HttpErrorResponse({ status: 500 }));

  it('creates a new admin with the name trimmed and shows a notice', () => {
    const component = fixture.componentInstance;
    service.createAdmin.and.returnValue(of({ ...STUDENT, id: 3, email: 'nova@teste.com', role: 'ADMIN' }));
    component.openCreate();
    component.form.setValue({ name: ' Nova Admin ', email: 'nova@teste.com', password: 'Senha@2026' });

    component.saveAdmin();

    expect(service.createAdmin).toHaveBeenCalledWith({ name: 'Nova Admin', email: 'nova@teste.com', password: 'Senha@2026' });
    expect(component.creating()).toBeFalse();
    expect(component.notice()).toContain('nova@teste.com');
  });

  it('keeps the form open with an error when creating fails', () => {
    const component = fixture.componentInstance;
    service.createAdmin.and.returnValue(fail());
    component.openCreate();
    component.form.setValue({ name: 'Nova', email: 'nova@teste.com', password: 'Senha@2026' });

    component.saveAdmin();

    expect(component.formError()).toBeTruthy();
    expect(component.saving()).toBeFalse();
    expect(component.creating()).toBeTrue();
  });

  it('shows errors when loading, changing the role or deleting fails', () => {
    const component = fixture.componentInstance;
    service.search.and.returnValue(fail());
    component.load(0);
    expect(component.error()).toContain('usuários');

    service.changeRole.and.returnValue(fail());
    component.confirming.set(STUDENT);
    component.confirmRoleChange();
    expect(component.error()).toContain('perfil');

    service.deleteUser.and.returnValue(fail());
    component.deleting.set(STUDENT);
    component.confirmDelete();
    expect(component.error()).toContain('excluir');
  });

  it('ignores confirmations without a selected user and clears the filters', () => {
    const component = fixture.componentInstance;
    component.confirmRoleChange();
    component.confirmDelete();
    expect(service.changeRole).not.toHaveBeenCalled();
    expect(service.deleteUser).not.toHaveBeenCalled();

    component.search = 'ana';
    component.role = 'ADMIN';
    service.search.calls.reset();
    component.clearFilters();
    expect(service.search).toHaveBeenCalledWith(jasmine.objectContaining({ search: '', role: '', page: 0 }));
  });
});
