import { TestBed } from '@angular/core/testing';

import { BarListComponent } from './bar-list.component';
import { ColumnChartComponent } from './column-chart.component';
import { LineChartComponent, niceMax } from './line-chart.component';

describe('charts', () => {
  it('rounds the axis top to a friendly number', () => {
    expect(niceMax(0)).toBe(4);
    expect(niceMax(7)).toBe(7);
    expect(niceMax(23)).toBe(30);
    expect(niceMax(140)).toBe(200);
  });

  it('draws one point per value in the line chart', () => {
    const fixture = TestBed.createComponent(LineChartComponent);
    fixture.componentRef.setInput('points', [{ label: 'a', value: 1 }, { label: 'b', value: 4 }, { label: 'c', value: 0 }]);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('circle').length).toBe(3);
  });

  it('scales bars against the largest value', () => {
    const fixture = TestBed.createComponent(BarListComponent);
    fixture.componentRef.setInput('items', [{ key: 'a', label: 'A', value: 5 }, { key: 'b', label: 'B', value: 0 }]);
    fixture.detectChanges();
    expect(fixture.componentInstance.percent(5)).toBe(100);
    expect(fixture.componentInstance.percent(0)).toBe(0);
  });

  it('hides the values and most date labels when there are many columns', () => {
    const fixture = TestBed.createComponent(ColumnChartComponent);
    const points = Array.from({ length: 30 }, (_, i) => ({ label: `d${i + 1}`, value: i }));
    fixture.componentRef.setInput('points', points);
    fixture.detectChanges();
    const labels = Array.from(fixture.nativeElement.querySelectorAll('span.text-\\[11px\\]') as NodeListOf<HTMLElement>)
      .map((s) => s.textContent?.trim()).filter(Boolean);
    expect(fixture.componentInstance.showValues()).toBeFalse();
    expect(labels).toEqual(['d1', 'd6', 'd11', 'd16', 'd21', 'd26', 'd30']);

    fixture.componentRef.setInput('points', points.slice(0, 6));
    fixture.detectChanges();
    expect(fixture.componentInstance.showValues()).toBeTrue();
    expect(fixture.componentInstance.showLabel(3)).toBeTrue();
  });

  it('hides the numbers above the line when there are more than 31 points', () => {
    const fixture = TestBed.createComponent(LineChartComponent);
    fixture.componentRef.setInput('points', Array.from({ length: 40 }, (_, i) => ({ label: `${i}`, value: 5 })));
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('text[font-weight="600"]').length).toBe(0);
  });
});
