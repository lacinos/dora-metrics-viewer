package com.gemini.dorametricsviewer.application;

import com.gemini.dorametricsviewer.domain.LeadTimeCalculator;
import com.gemini.dorametricsviewer.domain.TimeWindow;
import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
import com.gemini.dorametricsviewer.domain.port.MetricsRepositoryPort;
import com.gemini.dorametricsviewer.domain.port.SourceControlPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class DoraMetricsService {

    private final SourceControlPort sourceControlPort;
    private final MetricsRepositoryPort metricsRepositoryPort;
    private final LeadTimeCalculator leadTimeCalculator;

    public DoraMetricsService(SourceControlPort sourceControlPort,
                              MetricsRepositoryPort metricsRepositoryPort,
                              LeadTimeCalculator leadTimeCalculator) {
        this.sourceControlPort = sourceControlPort;
        this.metricsRepositoryPort = metricsRepositoryPort;
        this.leadTimeCalculator = leadTimeCalculator;
    }

    public DoraMetricsResult calculateMetrics(String repoUrl, TimeWindow timeWindow) {
        // 1. Fetch all raw data
        List<Deployment> deployments = sourceControlPort.fetchDeployments(repoUrl, timeWindow.start());
        List<Change> changes = sourceControlPort.fetchChanges(repoUrl, timeWindow.start());
        List<Incident> incidents = sourceControlPort.fetchIncidents(repoUrl, timeWindow.start());

        System.out.println("DEBUG: Fetched " + deployments.size() + " deployments");
        deployments.forEach(d -> {
            String bodySnippet = d.description() != null ? d.description().substring(0, Math.min(d.description().length(), 50)).replace("\n", " ") : "null";
            System.out.println("DEBUG: Deployment: " + d.id() + " at " + d.deployedAt() + " Body: " + bodySnippet + "...");
        });

        System.out.println("DEBUG: Fetched " + changes.size() + " changes");
        if (!changes.isEmpty()) {
            System.out.println("DEBUG: Newest Change: " + changes.get(0).id() + " mergedAt: " + changes.get(0).mergedAt());
            System.out.println("DEBUG: Oldest Change: " + changes.get(changes.size() - 1).id() + " mergedAt: " + changes.get(changes.size() - 1).mergedAt());
        }

        // 2. Persist (Optional for MVP, but good practice)
        metricsRepositoryPort.saveDeployments(deployments);
        metricsRepositoryPort.saveChanges(changes);
        // metricsRepositoryPort.saveIncidents(incidents); // Method missing in interface, skipping for now

        // 3. Calculate Lead Time
        Duration leadTime = leadTimeCalculator.calculate(changes, deployments);
        System.out.println("DEBUG: Calculated Lead Time: " + leadTime);

        // 4. Calculate Deployment Frequency
        // Logic: Total Deployments / Days in Window
        long daysInWindow = ChronoUnit.DAYS.between(timeWindow.start(), timeWindow.end());
        if (daysInWindow == 0) daysInWindow = 1; // Avoid division by zero
        double deploymentFrequency = (double) deployments.size() / daysInWindow;

        // 5. Calculate Change Failure Rate
        // Logic: (Incidents / Deployments) * 100
        // Simplification: We assume 1 incident = 1 failed deployment.
        double changeFailureRate = 0.0;
        if (!deployments.isEmpty()) {
            changeFailureRate = ((double) incidents.size() / deployments.size()) * 100.0;
        }

        // 6. Calculate Time to Restore Service
        // Logic: Average (ResolvedAt - CreatedAt)
        Duration timeToRestore = Duration.ZERO;
        if (!incidents.isEmpty()) {
            long totalSeconds = incidents.stream()
                    .mapToLong(i -> Duration.between(i.createdAt(), i.resolvedAt()).getSeconds())
                    .sum();
            timeToRestore = Duration.ofSeconds(totalSeconds / incidents.size());
        }

        return new DoraMetricsResult(
            leadTime,
            deploymentFrequency,
            changeFailureRate,
            timeToRestore
        );
    }
}