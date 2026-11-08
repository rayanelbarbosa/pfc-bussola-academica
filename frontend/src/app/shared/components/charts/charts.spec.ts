import { TestBed } from '@angular/core/testing';

import { BarListComponent } from './bar-list.component';
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
});
