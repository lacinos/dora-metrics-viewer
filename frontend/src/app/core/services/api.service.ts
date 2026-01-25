import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { DoraMetricsResult, ScanRequest, TimeWindow } from '../models/dora-metrics.model';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private readonly API_URL = '/api/metrics';

  scanRepository(repoUrl: string, timeWindow: TimeWindow): Observable<DoraMetricsResult> {
    const request: ScanRequest = { repoUrl, timeWindow };
    return this.http.post<DoraMetricsResult>(`${this.API_URL}/scan`, request);
  }
}
