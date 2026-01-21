import { Routes } from '@angular/router';
import { DashboardComponent } from './dashboard/dashboard.component';
import { HealthCheckComponent } from './health-check/health-check.component';

export const routes: Routes = [
  { path: '', component: DashboardComponent },
  { path: 'health', component: HealthCheckComponent }
];
