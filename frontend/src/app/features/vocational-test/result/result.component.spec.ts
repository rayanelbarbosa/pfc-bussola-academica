import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';

import { ResultComponent } from './result.component';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { CategoryScore, VocationalTestResult } from '../../../core/models/vocational-test.model';

describe('ResultComponent', () => {
  let service: jasmine.SpyObj<VocationalTestService>;

  const score = (category: string, label: string, value: number): CategoryScore => ({ category, label, score: value });
  const scores = [
    score('REALISTIC', 'Realista', 6),
    score('INVESTIGATIVE', 'Investigativo', 9),
    score('ARTISTIC', 'Artístico', 7),
    score('SOCIAL', 'Social', 8),
    score('ENTERPRISING', 'Empreendedor', 5),
    score('CONVENTIONAL', 'Convencional', 4)
  ];

  async function create(result: VocationalTestResult): Promise<ComponentFixture<ResultComponent>> {
    service = jasmine.createSpyObj('VocationalTestService', ['getResult']);
    service.getResult.and.returnValue(of(result));
    await TestBed.configureTestingModule({
      imports: [ResultComponent],
      providers: [provideRouter([]), { provide: VocationalTestService, useValue: service }]
    }).compileComponents();

    const fixture = TestBed.createComponent(ResultComponent);
    fixture.componentRef.setInput('id', String(result.id));
    fixture.detectChanges();
    return fixture;
  }

  it('shows the top profile, the 6 scores and the recommended areas', async () => {
    const fixture = await create({
      id: 3,
      createdAt: '2026-09-28T09:00:00',
      scores,
      topCategories: [scores[1]],
      recommendedAreas: ['Ciências Exatas', 'Ciências Biológicas', 'Medicina']
    });
    const el: HTMLElement = fixture.nativeElement;

    expect(service.getResult).toHaveBeenCalledWith(3);
    expect(el.querySelector('h1')?.textContent).toContain('Seu lado Investigativo se destaca');
    expect(el.querySelector('.top-score')?.textContent).toContain('9 de 10');
    expect(el.querySelectorAll('.score').length).toBe(6);
    expect(el.querySelectorAll('.area').length).toBe(3);
  });

  it('handles a tie in the top profile', async () => {
    const social = score('SOCIAL', 'Social', 9);
    const fixture = await create({
      id: 4,
      createdAt: '',
      scores,
      topCategories: [scores[1], social],
      recommendedAreas: ['Medicina', 'Pedagogia']
    });

    expect(fixture.nativeElement.querySelector('h1')?.textContent).toContain(
      'Seus lados Investigativo e Social se destacam'
    );
  });
});
