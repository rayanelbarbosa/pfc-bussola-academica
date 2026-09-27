import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { HomeComponent } from './home.component';

describe('HomeComponent', () => {
  it('shows the test card linking to the questionnaire', async () => {
    await TestBed.configureTestingModule({
      imports: [HomeComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    const fixture = TestBed.createComponent(HomeComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('h2')?.textContent).toContain('Descubra seu perfil vocacional');
    expect(el.querySelector('a.btn-accent')?.getAttribute('href')).toBe('/teste-vocacional');
    expect(el.querySelectorAll('.info-card').length).toBe(3);
  });
});
