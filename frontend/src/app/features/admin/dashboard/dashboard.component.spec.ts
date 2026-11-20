import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { Dashboard } from '../../../core/models/dashboard.model';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DashboardComponent } from './dashboard.component';

const DATA: Dashboard = {
  periodDays: 30,
  startDate: '2026-10-09',
  endDate: '2026-11-07',
  allTime: false,
  totalUsers: 12,
  newUsers: 5,
  newUsersPrevious: 2,
  totalTests: 20,
  tests: 8,
  testsPrevious: 10,
  mostCommonProfile: { key: 'SOCIAL', label: 'Social', count: 4 },
  mostChosenArea: { key: 'HEALTH', label: 'Saúde', count: 3 },
  testsPerDay: [{ date: '2026-11-06', count: 3 }, { date: '2026-11-07', count: 5 }],
  profileDistribution: [{ key: 'SOCIAL', label: 'Social', count: 4 }, { key: 'REALISTIC', label: 'Realista', count: 1 }],
  areaDistribution: [{ key: 'HEALTH', label: 'Saúde', count: 3 }, { key: 'EDUCATION', label: 'Educação', count: 1 }],
  newUsersPerWeek: [{ date: '2026-11-02', count: 5 }]
};

describe('DashboardComponent', () => {
  let fixture: ComponentFixture<DashboardComponent>;
  let service: jasmine.SpyObj<DashboardService>;

  beforeEach(async () => {
    service = jasmine.createSpyObj('DashboardService', ['getMetrics']);
    service.getMetrics.and.returnValue(of(DATA));
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [{ provide: DashboardService, useValue: service }]
    }).compileComponents();
    fixture = TestBed.createComponent(DashboardComponent);
    fixture.detectChanges();
  });

  it('shows the big numbers with the variation and the charts', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(service.getMetrics).toHaveBeenCalledWith(null, { from: null, to: null });
    // o número grande é o do período escolhido; o total fica na linha de baixo
    expect(el.querySelector('[data-testid="users-big-number"]')?.textContent?.trim()).toBe('5');
    expect(el.querySelector('[data-testid="tests-big-number"]')?.textContent?.trim()).toBe('8');
    expect(el.textContent).toContain('Novos cadastros no período');
    expect(el.textContent).toContain('12 no total');
    expect(el.textContent).toContain('20 no total');
    expect(el.textContent).toContain('+3 em relação aos 30 dias anteriores');
    expect(el.textContent).toContain('-2 em relação aos 30 dias anteriores');
    expect(el.textContent).toContain('Social');
    expect(el.textContent).toContain('Saúde');
    expect(el.querySelector('app-line-chart polyline')).toBeTruthy();
    expect(el.querySelectorAll('app-bar-list li').length).toBe(4);
  });

  it('starts with no quick period marked and toggles a quick period on and off', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('.chip-active')).toBeNull();

    fixture.componentInstance.selectPeriod(7);
    fixture.detectChanges();
    expect(service.getMetrics).toHaveBeenCalledWith(7, { from: null, to: null });
    expect(el.querySelector('.chip-active')?.textContent).toContain('7 dias');

    fixture.componentInstance.selectPeriod(7);
    fixture.detectChanges();
    expect(service.getMetrics).toHaveBeenCalledWith(null, { from: null, to: null });
    expect(el.querySelector('.chip-active')).toBeNull();
  });

  it('shows "desde" instead of a comparison when it is the whole period', () => {
    service.getMetrics.and.returnValue(of({ ...DATA, allTime: true, startDate: '2026-06-01' }));
    fixture.componentInstance.load();
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('desde 01/06/2026');
    expect(el.textContent).toContain('Usuários cadastrados');
    expect(el.querySelector('[data-testid="users-big-number"]')?.textContent?.trim()).toBe('12');
    expect(el.textContent).not.toContain('em relação');
  });

  it('shows the period and the calendar range unmarks the quick period', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('09/10/2026 a 07/11/2026');

    service.getMetrics.and.returnValue(of({ ...DATA, periodDays: 1, startDate: '2026-11-03', endDate: '2026-11-03' }));
    fixture.componentInstance.selectPeriod(30);
    fixture.componentInstance.selectRange({ from: '2026-11-03', to: '2026-11-03' });
    fixture.detectChanges();

    expect(service.getMetrics).toHaveBeenCalledWith(null, { from: '2026-11-03', to: '2026-11-03' });
    expect(el.textContent).toContain('03/11/2026');
    expect(el.textContent).toContain('em relação ao dia anterior');
    expect(el.querySelector('.chip-active')).toBeNull();
    expect(el.querySelector('app-date-range-picker button')?.textContent).toContain('03/11/2026');
  });
});
