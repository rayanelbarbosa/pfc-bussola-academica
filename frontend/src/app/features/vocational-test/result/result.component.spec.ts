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
    { category: 'REALISTIC', label: 'Realista', score: 18 },
    { category: 'INVESTIGATIVE', label: 'Investigativo', score: 8 },
    { category: 'ARTISTIC', label: 'Artístico', score: 14 },
    { category: 'SOCIAL', label: 'Social', score: 12 },
    { category: 'ENTERPRISING', label: 'Empreendedor', score: 6 },
    { category: 'CONVENTIONAL', label: 'Convencional', score: 18 }
  ],
  maxScore: 20,
  topCategories: [
    { category: 'REALISTIC', label: 'Realista', score: 18 },
    { category: 'CONVENTIONAL', label: 'Convencional', score: 18 }
  ],
  interestAreas: [{ value: 'ENGINEERING', label: 'Engenharias' }, { value: 'HEALTH', label: 'Saúde' }],
  careerRecommendations: [{
    name: 'Engenharia Civil', area: 'ENGINEERING', areaLabel: 'Engenharias', code: 'RIC',
    description: 'Projeta obras.', dailyTasks: ['Gerencia obras', 'Inspeciona projetos'],
    categories: [{ category: 'REALISTIC', label: 'Realista' }, { category: 'INVESTIGATIVE', label: 'Investigativo' }]
  }],
  suggestedCareers: [],
  allCareers: [
    {
      name: 'Engenharia Civil', area: 'ENGINEERING', areaLabel: 'Engenharias', code: 'RIC', description: 'Projeta obras.',
      dailyTasks: [], categories: [{ category: 'REALISTIC', label: 'Realista' }, { category: 'INVESTIGATIVE', label: 'Investigativo' }]
    },
    {
      name: 'Engenharia de Computação', area: 'ENGINEERING', areaLabel: 'Engenharias', code: 'RIC', description: 'Projeta hardware.',
      dailyTasks: [], categories: [{ category: 'REALISTIC', label: 'Realista' }, { category: 'INVESTIGATIVE', label: 'Investigativo' }]
    }
  ]
};

/** Perfil Artístico que escolheu só Engenharias: nenhuma engenharia tem Artístico como tipo principal. */
const POOR_MATCH: VocationalTestResult = {
  ...RESULT,
  topCategories: [{ category: 'ARTISTIC', label: 'Artístico', score: 14 }],
  interestAreas: [{ value: 'ENGINEERING', label: 'Engenharias' }],
  suggestedCareers: [{
    name: 'Artes Cênicas', area: 'ARTS_AND_COMMUNICATION', areaLabel: 'Artes e Comunicação', code: 'ASE',
    description: 'Atua no teatro.', dailyTasks: [],
    categories: [{ category: 'ARTISTIC', label: 'Artístico' }, { category: 'SOCIAL', label: 'Social' }]
  }]
};

describe('ResultComponent', () => {
  let fixture: ComponentFixture<ResultComponent>;
  let videoService: jasmine.SpyObj<VideoService>;
  let testService: jasmine.SpyObj<VocationalTestService>;

  beforeEach(async () => {
    testService = jasmine.createSpyObj('VocationalTestService', ['getResult']);
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

  it('explains the tied main profile at the top, then shows the scores and the career ranking', () => {
    const el: HTMLElement = fixture.nativeElement;
    const text = el.textContent ?? '';
    expect(text).toContain('Seu perfil é Realista e Convencional');
    // explicação de cada tipo empatado logo no topo
    expect(text).toContain('Realista:');
    expect(text).toContain('Convencional:');
    expect(el.querySelector('app-radar-chart svg polygon')).toBeTruthy();
    expect(text.indexOf('Seus pontos em cada tipo')).toBeLessThan(text.indexOf('Carreiras que combinam com você'));
    expect(text).toContain('Engenharia Civil');
    expect(text).toContain('Tipo principal: Realista');
    expect(text).toContain('você: 18/20');
    expect(text).toContain('Das áreas que você escolheu, Engenharias é a que mais combina com você');
    expect(text).not.toContain('No dia a dia');
    // áreas escolhidas combinam bem: sem aviso
    expect(text).not.toContain('como tipo principal.');
  });

  it('warns when the chosen areas fit poorly and lets the student switch lists', () => {
    testService.getResult.and.returnValue(of(POOR_MATCH));
    fixture = TestBed.createComponent(ResultComponent);
    fixture.componentRef.setInput('id', '7');
    fixture.detectChanges();
    const el: HTMLElement = fixture.nativeElement;

    // a escolha é respeitada: começa nas Engenharias, com o aviso
    expect(el.textContent).toContain('Nenhuma carreira das áreas que você escolheu tem Artístico como tipo principal');
    expect(el.textContent).toContain('a que mais combina com você é Artes Cênicas (Artes e Comunicação)');
    expect(el.textContent).not.toContain('Das áreas que você escolheu, Engenharias');
    expect(el.querySelector('h3')?.textContent).toContain('Engenharia Civil');

    const button = (label: string) => Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes(label))!;
    button('As que mais combinam comigo').click();
    fixture.detectChanges();
    expect(el.querySelector('h3')?.textContent).toContain('Artes Cênicas');
    expect(el.textContent).toContain('Carreiras que mais combinam com você');
    expect(videoService.searchByCareer).toHaveBeenCalledWith('Artes Cênicas', jasmine.any(Array));

    button('Das áreas que escolhi').click();
    fixture.detectChanges();
    expect(el.querySelector('h3')?.textContent).toContain('Engenharia Civil');
  });

  it('opens and closes "Mais sobre a carreira" with the daily tasks', () => {
    const el: HTMLElement = fixture.nativeElement;
    const button = Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Mais sobre a carreira'))!;
    button.click();
    fixture.detectChanges();
    expect(el.textContent).toContain('No dia a dia');
    expect(el.textContent).toContain('Gerencia obras');
    expect(el.textContent).toContain('usa os seus tipos Realista (18/20) e Investigativo (8/20)');
    expect(el.querySelector('a[href="/formas-de-ingresso"]')).toBeTruthy();
    fixture.componentInstance.toggleDetails('Engenharia Civil');
    fixture.detectChanges();
    expect(el.textContent).not.toContain('No dia a dia');
  });

  it('opens the full ranking with every career, in the same order, without loading their videos', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.querySelector('#ranking-completo')).toBeNull();
    const button = Array.from(el.querySelectorAll('button')).find((b) => b.textContent?.includes('Ver o ranking completo'))!;
    expect(button.textContent).toContain('(2 carreiras)');

    button.click();
    fixture.detectChanges();

    const items = el.querySelectorAll('#ranking-completo li');
    expect(items.length).toBe(2);
    expect(items[1].textContent).toContain('2º');
    expect(items[1].textContent).toContain('Engenharia de Computação');
    expect(items[1].textContent).toContain('você: 18/20 em Realista');
    expect(videoService.searchByCareer).not.toHaveBeenCalledWith('Engenharia de Computação', jasmine.any(Array));

    // vídeos só sob demanda, para não gastar a cota do YouTube com todas as carreiras
    const verVideos = Array.from(items[1].querySelectorAll('button')).find((b) => b.textContent?.includes('Ver vídeos'))!;
    verVideos.click();
    fixture.detectChanges();
    expect(videoService.searchByCareer).toHaveBeenCalledWith('Engenharia de Computação', jasmine.any(Array));
    expect(items[1].textContent).toContain('Vídeos indisponíveis no momento');
    expect(items[1].textContent).not.toContain('Ver vídeos');

    fixture.componentInstance.toggleShowAll();
    fixture.detectChanges();
    expect(el.querySelector('#ranking-completo')).toBeNull();
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
