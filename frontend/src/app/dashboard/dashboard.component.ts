import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/services/api.service';
import { DoraMetricsResult, TimeWindow } from '../core/models/dora-metrics.model';
import { NgxChartsModule, Color, ScaleType } from '@swimlane/ngx-charts';
import { catchError, finalize, of, tap } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxChartsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent {
  private apiService = inject(ApiService);

  repoUrl = signal<string>('');
  startDate = signal<string>(this.getDefaultStartDate());
  endDate = signal<string>(this.getDefaultEndDate());
  
  isLoading = signal<boolean>(false);
  error = signal<string | null>(null);
  metrics = signal<DoraMetricsResult | null>(null);

  // Chart Configuration
  blueScheme: Color = {
    name: 'blue',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#3b82f6']
  };

  greenScheme: Color = {
    name: 'green',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#10b981']
  };

  mixedScheme: Color = {
    name: 'mixed',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#10b981', '#ef4444']
  };

  amberScheme: Color = {
    name: 'amber',
    selectable: true,
    group: ScaleType.Ordinal,
    domain: ['#f59e0b']
  };

  private getDefaultStartDate(): string {
    const d = new Date();
    d.setDate(d.getDate() - 30);
    return d.toISOString().split('T')[0];
  }

  private getDefaultEndDate(): string {
    return new Date().toISOString().split('T')[0];
  }

  private parseDuration(ptString: string): number {
    if (!ptString || !ptString.startsWith('PT')) return 0;
    const hMatch = ptString.match(/(\d+)H/);
    const mMatch = ptString.match(/(\d+)M/);
    let hours = hMatch ? parseInt(hMatch[1], 10) : 0;
    let minutes = mMatch ? parseInt(mMatch[1], 10) : 0;
    return hours + (minutes / 60);
  }

  // Chart Data Signals
  leadTimeData = computed(() => {
    const m = this.metrics();
    if (!m) return [];
    
    return [
      {
        name: 'Lead Time (Hours)',
        value: this.parseDuration(m.leadTimeForChanges)
      }
    ];
  });

  deploymentFrequencyData = computed(() => {
    const m = this.metrics();
    if (!m) return [
      { name: 'Average', value: 0 }
    ];

    return [
      { name: 'Avg / Day', value: m.deploymentFrequency }
    ];
  });

  changeFailureRateData = computed(() => {
    const m = this.metrics();
    if (!m) return [
        { name: 'Success', value: 100 },
        { name: 'Failure', value: 0 }
    ];

    const failureRate = m.changeFailureRate;
    const successRate = 100 - failureRate;

    return [
      { name: 'Success', value: successRate },
      { name: 'Failure', value: failureRate }
    ];
  });

  timeToRestoreData = computed(() => {
    const m = this.metrics();
    if (!m) return [];

     return [
       { name: 'MTTR (Hours)', value: this.parseDuration(m.timeToRestoreService) }
     ];
  });

  onScan() {
    if (!this.repoUrl()) return;

    this.isLoading.set(true);
    this.error.set(null);
    this.metrics.set(null);

    const start = new Date(this.startDate());
    const end = new Date(this.endDate() + 'T23:59:59'); // Include the full end day
    
    const timeWindow: TimeWindow = {
      start: start.toISOString(),
      end: end.toISOString()
    };

    this.apiService.scanRepository(this.repoUrl(), timeWindow)
      .pipe(
        tap((res) => {
          this.metrics.set(res);
        }),
        catchError((err) => {
          this.error.set(err.message || 'Failed to scan repository');
          return of(null);
        }),
        finalize(() => {
          this.isLoading.set(false);
        })
      )
      .subscribe();
  }
}