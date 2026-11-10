import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { HowItWorksComponent } from './how-it-works.component';

describe('HowItWorksComponent', () => {
  it('explains the six RIASEC types, the hexagon and the three steps', async () => {
    await TestBed.configureTestingModule({ imports: [HowItWorksComponent], providers: [provideRouter([])] }).compileComponents();
    const fixture = TestBed.createComponent(HowItWorksComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('h1')?.textContent).toContain('Como funciona o teste');
    expect(el.querySelector('svg[role="img"]')).toBeTruthy();
    expect(el.querySelectorAll('ol > li').length).toBe(3);
    expect(el.textContent).toContain('Investigativo');
    expect(el.textContent).toContain('Exemplos:');
  });
});
