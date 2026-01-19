import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-health-check',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './health-check.component.html',
  styleUrl: './health-check.component.css'
})
export class HealthCheckComponent implements OnInit {
  status: string = 'DOWN';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.http.get<{status: string}>('/api/health').subscribe({
      next: (data) => this.status = data.status,
      error: (err) => console.error('API Error', err)
    });
  }
}