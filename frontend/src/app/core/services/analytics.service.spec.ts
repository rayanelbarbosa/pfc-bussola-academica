import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { Component } from '@angular/core';

import { AnalyticsService } from './analytics.service';

@Component({ standalone: true, template: '' })
class BlankComponent {}

describe('AnalyticsService', () => {
  let service: AnalyticsService;

  beforeEach(() => {
    delete window.dataLayer;
    spyOn(console, 'info');
    TestBed.configureTestingModule({
      providers: [provideRouter([{ path: 'teste-vocacional/resultado/:id', component: BlankComponent }])]
    });
    service = TestBed.inject(AnalyticsService);
  });

  it('pushes the event to the dataLayer and shows it in the console', () => {
    service.track('test_completed', { profile: 'S' });

    expect(window.dataLayer?.length).toBe(1);
    expect(window.dataLayer?.[0]).toEqual(jasmine.objectContaining({ event: 'test_completed', profile: 'S' }));
    expect(window.dataLayer?.[0]['event_time']).toEqual(jasmine.any(String));
    expect(console.info).toHaveBeenCalledWith('%c[evento] test_completed', jasmine.any(String), { profile: 'S' });
  });

  it('tracks page views without ids or query strings', async () => {
    service.start();
    service.start(); // não registra duas vezes
    await TestBed.inject(Router).navigateByUrl('/teste-vocacional/resultado/42?token=abc');

    expect(window.dataLayer).toEqual([jasmine.objectContaining({ event: 'page_view', page_path: '/teste-vocacional/resultado/:id' })]);
  });

  it('anonymizes paths', () => {
    expect(AnalyticsService.anonymizePath('/redefinir-senha?token=xyz')).toBe('/redefinir-senha');
    expect(AnalyticsService.anonymizePath('/admin/usuarios/7/editar#x')).toBe('/admin/usuarios/:id/editar');
    expect(AnalyticsService.anonymizePath('/')).toBe('/');
  });
});
