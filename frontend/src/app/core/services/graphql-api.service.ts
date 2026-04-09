import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { DoraMetricsResult, TimeWindow } from '../models/dora-metrics.model';

interface GraphQLResponse<T> {
  data: T;
  errors?: Array<{ message: string }>;
}

@Injectable({ providedIn: 'root' })
export class GraphqlApiService {
  private http = inject(HttpClient);
  private readonly GRAPHQL_URL = '/graphql';

  private readonly SCAN_MUTATION = `
    mutation ScanRepository($repoUrl: String!, $timeWindow: TimeWindowInput!) {
      scanRepository(repoUrl: $repoUrl, timeWindow: $timeWindow) {
        leadTimeForChanges
        deploymentFrequency
        changeFailureRate
        timeToRestoreService
      }
    }
  `;

  scanRepository(repoUrl: string, timeWindow: TimeWindow): Observable<DoraMetricsResult> {
    return this.http
      .post<GraphQLResponse<{ scanRepository: DoraMetricsResult }>>(this.GRAPHQL_URL, {
        query: this.SCAN_MUTATION,
        variables: { repoUrl, timeWindow }
      })
      .pipe(
        map(response => {
          if (response.errors?.length) {
            throw new Error(response.errors[0].message);
          }
          return response.data.scanRepository;
        })
      );
  }
}
