import { TestBed } from '@angular/core/testing';

import { RadarChartComponent } from './radar-chart.component';

describe('RadarChartComponent', () => {
  it('puts the minimum score at the center and the maximum on the border', () => {
    const fixture = TestBed.createComponent(RadarChartComponent);
    fixture.componentRef.setInput('points', [
      { label: 'A', value: 2 }, { label: 'B', value: 10 }, { label: 'C', value: 6 },
      { label: 'D', value: 2 }, { label: 'E', value: 2 }, { label: 'F', value: 2 }
    ]);
    fixture.detectChanges();
    const dots = fixture.componentInstance.dots();

    expect(dots[0]).toEqual({ x: 160, y: 160 });
    // eixo B (60°): raio inteiro (110) => x = 160 + 110 * cos(-30°)
    expect(dots[1].x).toBeCloseTo(255.3, 1);
    expect(fixture.nativeElement.querySelectorAll('text').length).toBeGreaterThan(6);
  });
});
