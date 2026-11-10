import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { PasswordToggleComponent } from './password-toggle.component';

@Component({
  standalone: true,
  imports: [PasswordToggleComponent],
  template: `<div class="relative"><input #campo type="password" /><app-password-toggle [target]="campo" /></div>`
})
class HostComponent {}

describe('PasswordToggleComponent', () => {
  it('alterna o campo entre senha escondida e visível', () => {
    const fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');

    expect(button.getAttribute('aria-label')).toBe('Mostrar senha');
    button.click();
    fixture.detectChanges();
    expect(input.type).toBe('text');
    expect(button.getAttribute('aria-label')).toBe('Esconder senha');

    button.click();
    fixture.detectChanges();
    expect(input.type).toBe('password');
  });
});
