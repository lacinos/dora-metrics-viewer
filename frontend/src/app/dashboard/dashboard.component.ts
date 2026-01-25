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

  // Chart Data Signals
  leadTimeData = computed(() => {
    const m = this.metrics();
    if (!m) return [];
    
    let val = 0;
    if (typeof m.leadTimeForChanges === 'string' && m.leadTimeForChanges.startsWith('PT')) {
       const hMatch = m.leadTimeForChanges.match(/(\d+)H/);
       const mMatch = m.leadTimeForChanges.match(/(\d+)M/);
       let hours = hMatch ? parseInt(hMatch[1], 10) : 0;
       let minutes = mMatch ? parseInt(mMatch[1], 10) : 0;
       val = hours + (minutes / 60);
    } else if (typeof m.leadTimeForChanges === 'number') {
       val = m.leadTimeForChanges / 3600; 
    }

    return [
      {
        name: 'Lead Time (Hours)',
        value: val
      }
    ];
  });

  deploymentFrequencyData = computed(() => {
      return [
        { name: 'Monday', value: 0 },
        { name: 'Tuesday', value: 0 },
        { name: 'Wednesday', value: 0 },
        { name: 'Thursday', value: 0 },
        { name: 'Friday', value: 0 },
        { name: 'Saturday', value: 0 },
        { name: 'Sunday', value: 0 }
      ];
  });

  changeFailureRateData = computed(() => {
    return [
      { name: 'Success', value: 100 },
      { name: 'Failure', value: 0 }
    ];
  });

  timeToRestoreData = computed(() => {
     return [
       { name: 'MTTR (Hours)', value: 0 }
     ];
  });

  onScan() {
    if (!this.repoUrl()) return;

    this.isLoading.set(true);
    this.error.set(null);
    this.metrics.set(null);

    const end = new Date();
    const start = new Date();
    start.setDate(start.getDate() - 30);
    
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
