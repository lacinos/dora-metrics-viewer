package com.gemini.dorametricsviewer.application;

import com.gemini.dorametricsviewer.domain.LeadTimeCalculator;
import com.gemini.dorametricsviewer.domain.TimeWindow;
import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    // -------------------------------------------------------------------------
    // Original tests
    // -------------------------------------------------------------------------

    @Test
    void calculateMetrics_shouldFetchSaveAndCalculate() {
        String repoUrl = "https://github.com/test/repo";
        Instant start = Instant.now().minus(Duration.ofDays(7));
        Instant end = Instant.now();
        TimeWindow timeWindow = new TimeWindow(start, end);

        List<Deployment> deployments = Collections.emptyList();
        List<Change> changes = Collections.emptyList();
        List<Incident> incidents = Collections.emptyList();
        Duration expectedLeadTime = Duration.ofHours(5);

        when(sourceControlPort.fetchDeployments(repoUrl, start)).thenReturn(deployments);
        when(sourceControlPort.fetchChanges(repoUrl, start)).thenReturn(changes);
        when(sourceControlPort.fetchIncidents(repoUrl, start)).thenReturn(incidents);

        DoraMetricsResult result = doraMetricsService.calculateMetrics(repoUrl, timeWindow);

        assertEquals(expectedLeadTime, result.leadTimeForChanges());

        verify(sourceControlPort).fetchDeployments(repoUrl, start);
        verify(sourceControlPort).fetchChanges(repoUrl, start);
        verify(metricsRepositoryPort).saveDeployments(deployments);
        verify(metricsRepositoryPort).saveChanges(changes);
    }

    @Test
    void calculateMetrics_shouldFilterOutDataOutsideWindow() {
        String repoUrl = "https://github.com/test/repo";
        Instant now = Instant.now();
        Instant start = now.minus(Duration.ofDays(10));
        Instant end = now.minus(Duration.ofDays(5));
        TimeWindow timeWindow = new TimeWindow(start, end);

        Deployment d1 = new Deployment("d1", repoUrl, "sha1", now.minus(Duration.ofDays(7)), now.minus(Duration.ofDays(7)), "env", "success", "desc");
        Change c1 = new Change("c1", repoUrl, "sha1", now.minus(Duration.ofDays(7)), now.minus(Duration.ofDays(7)), "author");
        Incident i1 = new Incident("i1", repoUrl, now.minus(Duration.ofDays(7)), now, "high", "desc");

        Deployment d2 = new Deployment("d2", repoUrl, "sha2", now.minus(Duration.ofDays(2)), now.minus(Duration.ofDays(2)), "env", "success", "desc");
        Change c2 = new Change("c2", repoUrl, "sha2", now.minus(Duration.ofDays(2)), now.minus(Duration.ofDays(2)), "author");
        Incident i2 = new Incident("i2", repoUrl, now.minus(Duration.ofDays(2)), now, "high", "desc");

        when(sourceControlPort.fetchDeployments(repoUrl, start)).thenReturn(List.of(d1, d2));
        when(sourceControlPort.fetchChanges(repoUrl, start)).thenReturn(List.of(c1, c2));
        when(sourceControlPort.fetchIncidents(repoUrl, start)).thenReturn(List.of(i1, i2));

        doraMetricsService.calculateMetrics(repoUrl, timeWindow);

        verify(metricsRepositoryPort).saveDeployments(List.of(d1));
        verify(metricsRepositoryPort).saveChanges(List.of(c1));
        verify(metricsRepositoryPort).saveIncidents(List.of(i1));
    }

    // -------------------------------------------------------------------------
    // Edge-case tests
    // -------------------------------------------------------------------------

    @Test
    void calculateMetrics_withNoDeployments_shouldReturnZeroFrequencyAndZeroCfr() {
        String repoUrl = "https://github.com/test/repo";
        Instant start = Instant.now().minus(Duration.ofDays(7));
        Instant end = Instant.now();
        TimeWindow timeWindow = new TimeWindow(start, end);

        when(sourceControlPort.fetchDeployments(repoUrl, start)).thenReturn(List.of());
        when(sourceControlPort.fetchChanges(repoUrl, start)).thenReturn(List.of());
        when(sourceControlPort.fetchIncidents(repoUrl, start)).thenReturn(List.of());

        DoraMetricsResult result = doraMetricsService.calculateMetrics(repoUrl, timeWindow);

        assertEquals(0.0, result.deploymentFrequency());
        assertEquals(0.0, result.changeFailureRate());
    }

    @Test
    void calculateMetrics_withDeploymentsAndIncidents_shouldCalculateChangeFailureRate() {
        String repoUrl = "https://github.com/test/repo";
        Instant start = Instant.now().minus(Duration.ofDays(7));
        Instant end = Instant.now();
        TimeWindow timeWindow = new TimeWindow(start, end);

        Instant within = start.plus(Duration.ofDays(3));

        List<Deployment> deployments = List.of(
            new Deployment("d1", repoUrl, "sha1", within, within, "prod", "SUCCESS", null),
            new Deployment("d2", repoUrl, "sha2", within, within, "prod", "SUCCESS", null),
            new Deployment("d3", repoUrl, "sha3", within, within, "prod", "SUCCESS", null),
            new Deployment("d4", repoUrl, "sha4", within, within, "prod", "SUCCESS", null)
        );
        List<Incident> incidents = List.of(
            new Incident("i1", repoUrl, within, within.plus(Duration.ofHours(2)), "high", "outage")
        );

        when(sourceControlPort.fetchDeployments(repoUrl, start)).thenReturn(deployments);
        when(sourceControlPort.fetchChanges(repoUrl, start)).thenReturn(List.of());
        when(sourceControlPort.fetchIncidents(repoUrl, start)).thenReturn(incidents);

        DoraMetricsResult result = doraMetricsService.calculateMetrics(repoUrl, timeWindow);

        // 1 incident / 4 deployments * 100 = 25.0%
        assertEquals(25.0, result.changeFailureRate());
    }

    @Test
    void calculateMetrics_shouldPropagateException_whenSourceControlPortFails() {
        String repoUrl = "https://github.com/test/repo";
        Instant start = Instant.now().minus(Duration.ofDays(7));
        Instant end = Instant.now();
        TimeWindow timeWindow = new TimeWindow(start, end);

        when(sourceControlPort.fetchDeployments(repoUrl, start))
                .thenThrow(new RuntimeException("GitHub API error"));

        assertThrows(RuntimeException.class,
                () -> doraMetricsService.calculateMetrics(repoUrl, timeWindow));

        // No partial save should have occurred
        verify(metricsRepositoryPort, never()).saveDeployments(any());
        verify(metricsRepositoryPort, never()).saveChanges(any());
    }
}
