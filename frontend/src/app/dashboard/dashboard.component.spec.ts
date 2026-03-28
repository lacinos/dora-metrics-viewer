import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, Subject, throwError } from 'rxjs';

import { DashboardComponent } from './dashboard.component';
import { ApiService } from '../core/services/api.service';
import { DoraMetricsResult } from '../core/models/dora-metrics.model';

const MOCK_RESULT: DoraMetricsResult = {
  leadTimeForChanges: 'PT1H',
  deploymentFrequency: 1.0,
  changeFailureRate: 0,
  timeToRestoreService: 'PT10M'
};

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let apiServiceSpy: jasmine.SpyObj<ApiService>;

  beforeEach(async () => {
    const spy = jasmine.createSpyObj('ApiService', ['scanRepository']);
    spy.scanRepository.and.returnValue(of(MOCK_RESULT));

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

  // -------------------------------------------------------------------------
  // Original tests
  // -------------------------------------------------------------------------

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
    expect(args[1].start).toContain('2023-01-01');
    expect(args[1].end).toContain('2023-01-31');
  });

  // -------------------------------------------------------------------------
  // Loading-state management
  // -------------------------------------------------------------------------

  it('should set isLoading to true while scan is in progress and false after', () => {
    const subject = new Subject<DoraMetricsResult>();
    apiServiceSpy.scanRepository.and.returnValue(subject.asObservable());

    component.repoUrl.set('https://github.com/test/repo');
    component.onScan();

    expect(component.isLoading()).toBeTrue();

    subject.next(MOCK_RESULT);
    subject.complete();

    expect(component.isLoading()).toBeFalse();
  });

  // -------------------------------------------------------------------------
  // Error-state management
  // -------------------------------------------------------------------------

  it('should set error signal when the API call fails', () => {
    apiServiceSpy.scanRepository.and.returnValue(
      throwError(() => new Error('Network error'))
    );

    component.repoUrl.set('https://github.com/test/repo');
    component.onScan();

    expect(component.error()).toBe('Network error');
    expect(component.isLoading()).toBeFalse();
  });

  it('should clear error signal at the start of a new scan', () => {
    // Prime with an existing error
    component.error.set('Previous error');

    component.repoUrl.set('https://github.com/test/repo');
    component.onScan();

    // error is cleared before the observable completes
    // (the spy resolves synchronously, so after onScan() the full flow ran)
    expect(component.error()).toBeNull();
  });

  // -------------------------------------------------------------------------
  // Computed chart data signals
  // -------------------------------------------------------------------------

  it('should compute leadTimeData from PT duration string', () => {
    component.metrics.set({
      ...MOCK_RESULT,
      leadTimeForChanges: 'PT2H30M'
    });

    expect(component.leadTimeData()).toEqual([{ name: 'Lead Time (Hours)', value: 2.5 }]);
  });

  it('should compute leadTimeData as zero when metrics are null', () => {
    component.metrics.set(null);

    expect(component.leadTimeData()).toEqual([{ name: 'Lead Time (Hours)', value: 0 }]);
  });

  it('should compute changeFailureRateData correctly', () => {
    component.metrics.set({ ...MOCK_RESULT, changeFailureRate: 30.0 });

    expect(component.changeFailureRateData()).toEqual([
      { name: 'Success', value: 70 },
      { name: 'Failure', value: 30 }
    ]);
  });

  it('should compute deploymentFrequencyData from metrics', () => {
    component.metrics.set({ ...MOCK_RESULT, deploymentFrequency: 2.5 });

    expect(component.deploymentFrequencyData()).toEqual([{ name: 'Avg / Day', value: 2.5 }]);
  });

  it('should compute timeToRestoreData from PT duration string', () => {
    component.metrics.set({ ...MOCK_RESULT, timeToRestoreService: 'PT45M' });

    expect(component.timeToRestoreData()).toEqual([{ name: 'MTTR (Hours)', value: 0.75 }]);
  });
});
