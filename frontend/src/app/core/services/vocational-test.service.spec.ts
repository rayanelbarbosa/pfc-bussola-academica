import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { VocationalTestService } from './vocational-test.service';
import { environment } from '../../../environments/environment';

describe('VocationalTestService', () => {
  const base = `${environment.apiUrl}/vocational-test`;
  let service: VocationalTestService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(VocationalTestService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('fetches the questions', () => {
    service.getQuestions().subscribe((questions) => expect(questions.length).toBe(1));
    http.expectOne(`${base}/questions`).flush([{ id: 1, statement: 'x', category: 'REALISTIC' }]);
  });

  it('submits the answers via POST', () => {
    const body = { answers: [{ questionId: 1, score: 5 }] };
    service.submitAnswers(body).subscribe();

    const req = http.expectOne(`${base}/results`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(body);
    req.flush({});
  });

  it('fetches a result by id', () => {
    service.getResult(7).subscribe((r) => expect(r.id).toBe(7));
    http.expectOne(`${base}/results/7`).flush({ id: 7 });
  });
});
