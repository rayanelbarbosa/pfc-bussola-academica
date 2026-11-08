import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { Dashboard } from '../../../core/models/dashboard.model';
import { DashboardService } from '../../../core/services/dashboard.service';
import { DashboardComponent } from './dashboard.component';

const DATA: Dashboard = {
  periodDays: 7,
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
    expect(service.getMetrics).toHaveBeenCalledWith(30);
    expect(el.textContent).toContain('12');
    expect(el.textContent).toContain('+3 em relação aos 30 dias anteriores');
    expect(el.textContent).toContain('-2 em relação aos 30 dias anteriores');
    expect(el.textContent).toContain('Social');
    expect(el.textContent).toContain('Saúde');
    expect(el.querySelector('app-line-chart polyline')).toBeTruthy();
    expect(el.querySelectorAll('app-bar-list li').length).toBe(4);
  });

  it('reloads when the period changes', () => {
    fixture.componentInstance.selectPeriod(7);
    expect(service.getMetrics).toHaveBeenCalledWith(7);
  });
});
