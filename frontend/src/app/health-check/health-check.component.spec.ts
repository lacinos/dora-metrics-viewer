import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { HealthCheckComponent } from './health-check.component';

describe('HealthCheckComponent', () => {
  let component: HealthCheckComponent;
  let fixture: ComponentFixture<HealthCheckComponent>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HealthCheckComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HealthCheckComponent);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    // detectChanges is called in each test so we control when ngOnInit fires
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/health').flush({ status: 'UP' });
    expect(component).toBeTruthy();
  });

  it('should call GET /api/health on init', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/health');
    expect(req.request.method).toBe('GET');
    req.flush({ status: 'UP' });
  });

  it('should set status to UP when the API responds with UP', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/health');
    req.flush({ status: 'UP' });

    expect(component.status).toBe('UP');
  });

  it('should keep status as DOWN when the API call errors', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/health');
    req.flush('Service unavailable', { status: 503, statusText: 'Service Unavailable' });

    expect(component.status).toBe('DOWN');
  });
});
