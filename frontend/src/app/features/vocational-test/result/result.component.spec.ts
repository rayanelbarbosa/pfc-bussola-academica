import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';

import { VocationalTestResult } from '../../../core/models/vocational-test.model';
import { VideoService } from '../../../core/services/video.service';
import { VocationalTestService } from '../../../core/services/vocational-test.service';
import { ResultComponent } from './result.component';

const RESULT: VocationalTestResult = {
  id: 7,
  createdAt: '2026-11-07T20:00:00',
  scores: [
    { category: 'REALISTIC', label: 'Realista', score: 9 },
    { category: 'INVESTIGATIVE', label: 'Investigativo', score: 4 },
    { category: 'ARTISTIC', label: 'Artístico', score: 7 },
    { category: 'SOCIAL', label: 'Social', score: 6 },
    { category: 'ENTERPRISING', label: 'Empreendedor', score: 3 },
    { category: 'CONVENTIONAL', label: 'Convencional', score: 9 }
  ],
  topCategories: [
    { category: 'REALISTIC', label: 'Realista', score: 9 },
    { category: 'CONVENTIONAL', label: 'Convencional', score: 9 }
  ],
  interestAreas: [{ value: 'ENGINEERING', label: 'Engenharias' }],
  careerRecommendations: [{
    name: 'Engenharia Civil', area: 'ENGINEERING', areaLabel: 'Engenharias', code: 'RI', compatibility: 22,
    description: 'Projeta obras.', tiedWith: 'Engenharia Mecânica', tieReason: 'PROFILE',
    categories: [{ category: 'REALISTIC', label: 'Realista' }, { category: 'INVESTIGATIVE', label: 'Investigativo' }]
  }],
  areaMatches: [
    { area: 'ENGINEERING', label: 'Engenharias', bestCareer: 'Engenharia Civil', bestCompatibility: 22,
      bestCategories: [{ category: 'REALISTIC', label: 'Realista' }, { category: 'INVESTIGATIVE', label: 'Investigativo' }] },
    { area: 'HEALTH', label: 'Saúde', bestCareer: 'Enfermagem', bestCompatibility: 18,
      bestCategories: [{ category: 'SOCIAL', label: 'Social' }, { category: 'INVESTIGATIVE', label: 'Investigativo' }] }
  ]
};

describe('ResultComponent', () => {
  let fixture: ComponentFixture<ResultComponent>;
  let videoService: jasmine.SpyObj<VideoService>;

  beforeEach(async () => {
    const testService = jasmine.createSpyObj('VocationalTestService', ['getResult']);
    testService.getResult.and.returnValue(of(RESULT));
    videoService = jasmine.createSpyObj('VideoService', ['searchByCareer']);
    videoService.searchByCareer.and.returnValue(throwError(() => new Error('cota')));
    await TestBed.configureTestingModule({
      imports: [ResultComponent],
      providers: [
        provideRouter([]),
        { provide: VocationalTestService, useValue: testService },
        { provide: VideoService, useValue: videoService }
      ]
    }).compileComponents();
    fixture = TestBed.createComponent(ResultComponent);
    fixture.componentRef.setInput('id', '7');
    fixture.detectChanges();
  });

  it('shows the radar chart, the tied main profile with explanations and the single career ranking', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('app-radar-chart svg polygon')).toBeTruthy();
    expect(el.textContent).toContain('2 tipos tiveram o mesmo maior escore');
    expect(el.textContent).toContain('Características comuns');
    expect(el.textContent).toContain('Engenharia Civil');
    expect(el.textContent).toContain('22 de 30 pontos');
    expect(el.textContent).toContain('1º · Engenharias');
    expect(el.textContent).toContain('Seu perfil combina mais com Engenharias');
    expect(el.textContent).toContain('Enfermagem pede Social');
    expect(el.textContent).toContain('Empatou com Engenharia Mecânica');
    expect(el.textContent).toContain('Seu perfil é Realista e Convencional');
  });

  it('loads the videos of each career and keeps the page working when the API is down', () => {
    expect(videoService.searchByCareer).toHaveBeenCalledWith('Engenharia Civil', jasmine.any(Array));
    // perfil = 2 tipos de maior escore, mandados para o ranking dos vídeos
    expect(fixture.componentInstance.videoProfile()).toEqual(['REALISTIC', 'CONVENTIONAL']);
    expect(fixture.nativeElement.textContent).toContain('Vídeos indisponíveis no momento');
  });

  it('shows the videos found', () => {
    videoService.searchByCareer.and.returnValue(of([
      { videoId: 'a1', title: 'Rotina de engenheira civil', channelTitle: 'Canal', thumbnailUrl: null, url: 'https://youtube.com/watch?v=a1' }
    ]));
    fixture.componentInstance.loadVideos('Engenharia Civil');
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('a[href="https://youtube.com/watch?v=a1"]')).toBeTruthy();
  });
});
