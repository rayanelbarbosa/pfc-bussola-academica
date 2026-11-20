import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DateRangePickerComponent, DateRangeValue, formatBr, toIso } from './date-range-picker.component';

@Component({
  standalone: true,
  imports: [DateRangePickerComponent],
  template: `<app-date-range-picker label="Período" [from]="from()" [to]="to()" (rangeChange)="changes.push($event)" />
    <p id="fora">fora</p>`
})
class HostComponent {
  readonly from = signal<string | null>(null);
  readonly to = signal<string | null>(null);
  readonly changes: DateRangeValue[] = [];
}

/** Dia do mês anterior (todos os dias dele já passaram, então estão habilitados). */
function lastMonth(day: number): string {
  const date = new Date();
  date.setDate(1);
  date.setMonth(date.getMonth() - 1);
  date.setDate(day);
  return toIso(date);
}

describe('DateRangePickerComponent', () => {
  let fixture: ComponentFixture<HostComponent>;
  let el: HTMLElement;

  const trigger = () => el.querySelector<HTMLButtonElement>('app-date-range-picker > button')!;
  const panel = () => el.querySelector('[role="dialog"]');
  const day = (iso: string) => el.querySelector<HTMLButtonElement>(`[data-date="${iso}"]`)!;
  const click = (button: HTMLElement) => {
    button.click();
    fixture.detectChanges();
  };
  const openOnLastMonth = () => {
    click(trigger());
    click(el.querySelector<HTMLButtonElement>('[aria-label="Mês anterior"]')!);
  };

  beforeEach(() => {
    fixture = TestBed.createComponent(HostComponent);
    el = fixture.nativeElement;
    fixture.detectChanges();
  });

  it('shows "Todo o período" without dates and the chosen period when it has dates', () => {
    expect(trigger().textContent).toContain('Todo o período');
    fixture.componentInstance.from.set('2026-11-01');
    fixture.componentInstance.to.set('2026-11-20');
    fixture.detectChanges();
    expect(trigger().textContent).toContain('01/11/2026 a 20/11/2026');
    fixture.componentInstance.from.set('2026-11-20');
    fixture.detectChanges();
    expect(trigger().textContent).toContain('20/11/2026');
    expect(trigger().textContent).not.toContain(' a ');
  });

  it('opens the calendar below the button and closes it with Esc or a click outside', () => {
    click(trigger());
    expect(panel()).toBeTruthy();
    expect(trigger().getAttribute('aria-expanded')).toBe('true');

    panel()!.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    fixture.detectChanges();
    expect(panel()).toBeNull();

    click(trigger());
    click(el.querySelector<HTMLElement>('#fora')!);
    expect(panel()).toBeNull();
  });

  it('applies the period on the second click, in date order, and closes', () => {
    openOnLastMonth();
    click(day(lastMonth(20)));
    expect(panel()).toBeTruthy();
    expect(fixture.componentInstance.changes).toEqual([]);

    click(day(lastMonth(5)));
    expect(fixture.componentInstance.changes).toEqual([{ from: lastMonth(5), to: lastMonth(20) }]);
    expect(panel()).toBeNull();
  });

  it('filters a single day when the same day is clicked twice', () => {
    openOnLastMonth();
    click(day(lastMonth(10)));
    click(day(lastMonth(10)));
    expect(fixture.componentInstance.changes).toEqual([{ from: lastMonth(10), to: lastMonth(10) }]);
  });

  it('disables future days and does not go past the current month', () => {
    click(trigger());
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const future = day(toIso(tomorrow));
    if (future) {
      expect(future.disabled).toBeTrue();
    }
    expect(el.querySelector<HTMLButtonElement>('[aria-label="Próximo mês"]')!.disabled).toBeTrue();
    expect(day(toIso(new Date())).disabled).toBeFalse();
  });

  it('clears the period', () => {
    fixture.componentInstance.from.set('2026-11-01');
    fixture.componentInstance.to.set('2026-11-20');
    fixture.detectChanges();
    click(trigger());
    const allTime = () => Array.from(el.querySelectorAll<HTMLButtonElement>('button')).find((b) => b.textContent?.trim() === 'Todo o período')!;
    expect(allTime().getAttribute('aria-pressed')).toBe('false');
    click(allTime());
    expect(fixture.componentInstance.changes).toEqual([{ from: null, to: null }]);
    expect(panel()).toBeNull();
  });

  it('moves between days with the arrow keys and labels each day in full', () => {
    openOnLastMonth();
    const start = day(lastMonth(10));
    expect(start.getAttribute('aria-label')).toMatch(/^10 de [a-zç]+ de \d{4}$/);
    start.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    fixture.detectChanges();
    expect(day(lastMonth(11)).getAttribute('tabindex')).toBe('0');
  });

  it('formats ISO dates in the Brazilian format', () => {
    expect(formatBr('2026-06-01')).toBe('01/06/2026');
  });
});
