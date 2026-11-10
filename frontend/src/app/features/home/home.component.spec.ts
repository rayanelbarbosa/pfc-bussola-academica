import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  it('presents the site and links to the admission guide and to the test explanation', async () => {
    await TestBed.configureTestingModule({ imports: [HomeComponent], providers: [provideRouter([])] }).compileComponents();
    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('h1')?.textContent).toContain('Como entrar na faculdade');
    expect(el.querySelector('a[href="/formas-de-ingresso"]')).toBeTruthy();
    expect(el.querySelector('a[href="/como-funciona"]')).toBeTruthy();
    expect(el.querySelector('a[href="/teste-vocacional"]')).toBeTruthy();
    expect(el.textContent).toContain('Prouni');
  });
});
