package com.gemini.dorametricsviewer.infrastructure.github.mock;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
import com.gemini.dorametricsviewer.domain.port.SourceControlPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * A Fake implementation of SourceControlPort for testing purposes.
 * Populated with a "Golden Dataset" to verify DORA metric calculations.
 */
public class FakeGitHubAdapter implements SourceControlPort {

    private final Instant now = Instant.now();

    @Override
    public List<Deployment> fetchDeployments(String repoUrl, Instant since) {
        // Golden Dataset: 2 Deployments
        // D1: 6 days ago. SHA-1.
        // D2: 2 days ago. SHA-2.
        return List.of(
            new Deployment("dep-1", repoUrl, "sha-1", now.minus(6, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS), "production", "SUCCESS"),
            new Deployment("dep-2", repoUrl, "sha-2", now.minus(2, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS), "production", "SUCCESS")
        );
    }

    @Override
    public List<Change> fetchChanges(String repoUrl, Instant since) {
        // Golden Dataset: 4 Changes
        // To satisfy the simplistic LeadTimeCalculator (Change.sha == Deployment.sha),
        // we map these changes to the deployment SHAs.
        
        // Associated with D1 (6 days ago):
        // C1: Created 7 days ago. (Lead Time: 1 day)
        // C2: Created 7 days ago. (Lead Time: 1 day)
        
        // Associated with D2 (2 days ago):
        // C3: Created 5 days ago. (Lead Time: 3 days)
        // C4: Created 5 days ago. (Lead Time: 3 days)
        
        // Avg Lead Time = (1+1+3+3)/4 = 2 days.
        return List.of(
            new Change("pr-1", repoUrl, "sha-1", now.minus(7, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS).minusSeconds(1), "user1"),
            new Change("pr-2", repoUrl, "sha-1", now.minus(7, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS).minusSeconds(1), "user2"),
            new Change("pr-3", repoUrl, "sha-2", now.minus(5, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS).minusSeconds(1), "user3"),
            new Change("pr-4", repoUrl, "sha-2", now.minus(5, ChronoUnit.DAYS), now.minus(2, ChronoUnit.DAYS).minusSeconds(1), "user4")
        );
    }

    @Override
    public List<Incident> fetchIncidents(String repoUrl, Instant since) {
        // Golden Dataset: 1 Incident
        // Caused by D1 (6 days ago)
        // Created: 6 days ago (shortly after deployment)
        // Resolved: 4 hours later
        // Time to Restore: 4 hours
        Instant created = now.minus(6, ChronoUnit.DAYS).plus(10, ChronoUnit.MINUTES);
        Instant resolved = created.plus(4, ChronoUnit.HOURS);
        
        return List.of(
            new Incident("issue-1", repoUrl, created, resolved, "high", "Bug in production")
        );
    }
}