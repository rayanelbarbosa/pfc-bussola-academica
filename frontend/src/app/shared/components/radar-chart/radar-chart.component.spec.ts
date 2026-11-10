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

    expect(dots[0]).toEqual({ x: 160, y: 160, color: 'var(--primary)' });
    // eixo B (60°): raio inteiro (110) => x = 160 + 110 * cos(-30°)
    expect(dots[1].x).toBeCloseTo(255.3, 1);
    expect(fixture.nativeElement.querySelectorAll('text').length).toBeGreaterThan(6);
  });

  it('pinta cada ponto com a cor informada e deixa margem para os rótulos laterais', () => {
    const fixture = TestBed.createComponent(RadarChartComponent);
    fixture.componentRef.setInput('points', [
      { label: 'Realista', value: 4, color: 'var(--riasec-r)' }, { label: 'Investigativo', value: 8, color: 'var(--riasec-i)' },
      { label: 'Artístico', value: 6 }, { label: 'Social', value: 10 }, { label: 'Empreendedor', value: 2 },
      { label: 'Convencional', value: 2 }
    ]);
    fixture.detectChanges();

    expect(fixture.componentInstance.dots()[1].color).toBe('var(--riasec-i)');
    expect(fixture.componentInstance.axes()[0].color).toBe('var(--riasec-r)');
    // viewBox mais largo que o gráfico (320): "Investigativo" e "Empreendedor" não são cortados
    const svg: SVGElement = fixture.nativeElement.querySelector('svg');
    expect(svg.getAttribute('viewBox')).toBe('-80 -16 480 352');
  });
});
