import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  it('renders the router outlet and the footer with the legal links', async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [provideRouter([])]
    }).compileComponents();

    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    expect(el.querySelector('router-outlet')).toBeTruthy();
    expect(el.querySelector('a[href="/termos-de-uso"]')).toBeTruthy();
    expect(el.querySelector('a[href="/politica-de-privacidade"]')).toBeTruthy();
  });
});
