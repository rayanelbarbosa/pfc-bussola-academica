import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { MyDataComponent } from './my-data.component';
import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';
import { authResponse } from '../../../core/testing/auth-fixtures';

describe('MyDataComponent', () => {
  let fixture: ComponentFixture<MyDataComponent>;
  let userService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    userService = jasmine.createSpyObj('UserService', ['getMe', 'getMyResults', 'deleteMyAccount', 'exportMyData']);
    userService.getMe.and.returnValue(of(authResponse().user));
    userService.getMyResults.and.returnValue(of([{ id: 3, createdAt: '2026-09-27T10:00:00', topCategories: ['Social'], interestAreas: ['Saúde'] }]));

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
    expect(el.textContent).toContain('1 teste guardado');
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

  it('downloads the exported data as a JSON file', () => {
    userService.exportMyData.and.returnValue(of(new Blob(['{}'], { type: 'application/json' })));
    const click = spyOn(HTMLAnchorElement.prototype, 'click');
    spyOn(URL, 'createObjectURL').and.returnValue('blob:teste');
    const revoke = spyOn(URL, 'revokeObjectURL');

    fixture.componentInstance.downloadMyData();

    expect(click).toHaveBeenCalled();
    expect(revoke).toHaveBeenCalledWith('blob:teste');
    expect(fixture.componentInstance.exporting()).toBeFalse();
  });

  it('shows an error when the export fails', () => {
    userService.exportMyData.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.componentInstance.downloadMyData();
    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.componentInstance.exporting()).toBeFalse();
  });

  it('keeps the account and shows an error when the deletion fails', () => {
    userService.deleteMyAccount.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.componentInstance.deleteAccount();
    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.componentInstance.deleting()).toBeFalse();
  });

  it('shows an error when the data cannot be loaded and an empty history when results fail', () => {
    userService.getMe.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    userService.getMyResults.and.returnValue(throwError(() => new HttpErrorResponse({ status: 500 })));
    fixture.componentInstance.ngOnInit();
    expect(fixture.componentInstance.error()).toBeTruthy();
    expect(fixture.componentInstance.results()).toEqual([]);
  });
});
