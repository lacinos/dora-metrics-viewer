package com.gemini.dorametricsviewer.application;

import com.gemini.dorametricsviewer.domain.LeadTimeCalculator;
import com.gemini.dorametricsviewer.domain.TimeWindow;
import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.port.MetricsRepositoryPort;
import com.gemini.dorametricsviewer.domain.port.SourceControlPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoraMetricsServiceTest {

    @Mock
    private SourceControlPort sourceControlPort;

    @Mock
    private MetricsRepositoryPort metricsRepositoryPort;

    private LeadTimeCalculator leadTimeCalculator;

    private DoraMetricsService doraMetricsService;

    @BeforeEach
    void setUp() {
        // Use a stub subclass to avoid ByteBuddy/Java 25 issues with Mockito on concrete classes
        leadTimeCalculator = new LeadTimeCalculator() {
            @Override
            public Duration calculate(List<Change> changes, List<Deployment> deployments) {
                return Duration.ofHours(5);
            }
        };
        doraMetricsService = new DoraMetricsService(sourceControlPort, metricsRepositoryPort, leadTimeCalculator);
    }

    @Test
    void calculateMetrics_shouldFetchSaveAndCalculate() {
        // Given
        String repoUrl = "https://github.com/test/repo";
        Instant start = Instant.now().minus(Duration.ofDays(7));
        Instant end = Instant.now();
        TimeWindow timeWindow = new TimeWindow(start, end);

        List<Deployment> deployments = Collections.emptyList(); // Just for flow verification
        List<Change> changes = Collections.emptyList();
        Duration expectedLeadTime = Duration.ofHours(5);

        when(sourceControlPort.fetchDeployments(repoUrl, start)).thenReturn(deployments);
        when(sourceControlPort.fetchChanges(repoUrl, start)).thenReturn(changes);
        // No when(leadTimeCalculator...) needed because it's a stub

        // When
        DoraMetricsResult result = doraMetricsService.calculateMetrics(repoUrl, timeWindow);

        // Then
        assertEquals(expectedLeadTime, result.leadTimeForChanges());

        verify(sourceControlPort).fetchDeployments(repoUrl, start);
        verify(sourceControlPort).fetchChanges(repoUrl, start);
        verify(metricsRepositoryPort).saveDeployments(deployments);
        verify(metricsRepositoryPort).saveChanges(changes);
    }
}
