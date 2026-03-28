import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ApiService } from './api.service';
import { DoraMetricsResult, TimeWindow } from '../models/dora-metrics.model';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  const timeWindow: TimeWindow = {
    start: '2023-01-01T00:00:00.000Z',
    end: '2023-01-31T23:59:59.000Z'
  };
  const repoUrl = 'https://github.com/owner/repo';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ApiService
      ]
    });

    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should issue a POST to /api/metrics/scan', () => {
    service.scanRepository(repoUrl, timeWindow).subscribe();

    const req = httpMock.expectOne('/api/metrics/scan');
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should send the correct request body', () => {
    service.scanRepository(repoUrl, timeWindow).subscribe();

    const req = httpMock.expectOne('/api/metrics/scan');
    expect(req.request.body).toEqual({ repoUrl, timeWindow });
    req.flush({});
  });

  it('should return the parsed DoraMetricsResult on success', () => {
    const mockResult: DoraMetricsResult = {
      leadTimeForChanges: 'PT2H',
      deploymentFrequency: 1.5,
      changeFailureRate: 10.0,
      timeToRestoreService: 'PT30M'
    };

    let received: DoraMetricsResult | undefined;
    service.scanRepository(repoUrl, timeWindow).subscribe(r => (received = r));

    const req = httpMock.expectOne('/api/metrics/scan');
    req.flush(mockResult);

    expect(received).toEqual(mockResult);
  });

  it('should propagate an error Observable on HTTP failure', () => {
    let caughtError: any;
    service.scanRepository(repoUrl, timeWindow).subscribe({
      next: () => fail('expected error, not success'),
      error: e => (caughtError = e)
    });

    const req = httpMock.expectOne('/api/metrics/scan');
    req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });

    expect(caughtError).toBeTruthy();
    expect(caughtError.status).toBe(500);
  });
});
