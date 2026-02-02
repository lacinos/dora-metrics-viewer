import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { ApiService } from '../core/services/api.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('ApiService', ['scanRepository']);
    // Setup spy to return an Observable
    spy.scanRepository.and.returnValue(of({
      leadTimeForChanges: 'PT1H',
      deploymentFrequency: 1.0,
      changeFailureRate: 0,
      timeToRestoreService: 'PT10M'
    }));

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: ApiService, useValue: spy },
        provideNoopAnimations()
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    apiServiceSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call scanRepository with correct dates when onScan is called', () => {
    component.repoUrl.set('https://github.com/test/repo');
    component.startDate.set('2023-01-01');
    component.endDate.set('2023-01-31');

    component.onScan();

    expect(apiServiceSpy.scanRepository).toHaveBeenCalled();
    const args = apiServiceSpy.scanRepository.calls.mostRecent().args;
    expect(args[0]).toBe('https://github.com/test/repo');
    
    // Check start date (ISO string contains date)
    expect(args[1].start).toContain('2023-01-01');
    // Check end date (ISO string contains date)
    expect(args[1].end).toContain('2023-01-31');
  });
});
