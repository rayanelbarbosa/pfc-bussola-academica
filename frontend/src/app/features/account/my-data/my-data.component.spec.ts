import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';

import { MyDataComponent } from './my-data.component';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { authResponse } from '../../../core/testing/auth-fixtures';

describe('MyDataComponent', () => {
  let fixture: ComponentFixture<MyDataComponent>;
  let userService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    userService = jasmine.createSpyObj('UserService', ['getMe', 'getMyResults', 'deleteMyAccount']);
    userService.getMe.and.returnValue(of(authResponse().user));
    userService.getMyResults.and.returnValue(of([{ id: 3, createdAt: '2026-09-27T10:00:00', topCategories: ['Social'] }]));

    await TestBed.configureTestingModule({
      imports: [MyDataComponent],
      providers: [provideRouter([]), provideHttpClient(), { provide: UserService, useValue: userService }]
    }).compileComponents();
    fixture = TestBed.createComponent(MyDataComponent);
    fixture.detectChanges();
  });

  it('shows the personal data and the results history', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('marina@teste.com');
    expect(el.textContent).toContain('Versão 1.0');
    expect(el.querySelectorAll('.results li').length).toBe(1);
  });

  it('asks for confirmation and then deletes the account', () => {
    const router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
    const auth = TestBed.inject(AuthService);
    spyOn(auth, 'clear');
    userService.deleteMyAccount.and.returnValue(of(undefined));

    fixture.componentInstance.confirmingDeletion.set(true);
    fixture.componentInstance.deleteAccount();

    expect(userService.deleteMyAccount).toHaveBeenCalled();
    expect(auth.clear).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
