import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { UserService } from '../../../core/services/user.service';
import { MyResultsComponent } from './my-results.component';

describe('MyResultsComponent', () => {
  async function render(results: unknown[]) {
    const userService = jasmine.createSpyObj('UserService', ['getMyResults']);
    userService.getMyResults.and.returnValue(of(results));
    await TestBed.configureTestingModule({
      imports: [MyResultsComponent],
      providers: [provideRouter([]), { provide: UserService, useValue: userService }]
    }).compileComponents();
    const fixture = TestBed.createComponent(MyResultsComponent);
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('lists each test with its areas and main profile', async () => {
    const el = await render([{ id: 3, createdAt: '2026-11-07T10:00:00', topCategories: ['Social'], interestAreas: ['Saúde', 'Educação'] }]);
    expect(el.textContent).toContain('Saúde, Educação');
    expect(el.textContent).toContain('Social');
    expect(el.querySelector('a[href="/teste-vocacional/resultado/3"]')).toBeTruthy();
  });

  it('shows an empty state inviting to take the test', async () => {
    const el = await render([]);
    expect(el.textContent).toContain('Você ainda não fez nenhum teste');
  });
});
