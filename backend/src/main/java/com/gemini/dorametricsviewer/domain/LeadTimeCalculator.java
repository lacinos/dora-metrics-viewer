package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class LeadTimeCalculator {

    private final List<MatchStrategy> strategies = List.of(
        new ExactMatchStrategy(),
        new ReleaseBodyStrategy(),
        new TimeWindowStrategy()
    );

    public Duration calculate(List<Change> changes, List<Deployment> deployments) {
        List<Duration> leadTimes = changes.stream()
            .map(change -> findDeploymentFor(change, deployments))
            .filter(Optional::isPresent)
            .map(opt -> {
                 // Calculate Lead Time: from code commit (creation) to deployment
                 // If deployment happened before creation (impossible theoretical, but possible with bad data/time window), we take absolute or 0?
                 // Duration.between handles negatives, but we usually want positive.
                 // We trust the strategies to return valid deployments (after merge).
                 // Note: We need the change object here too.
                 // Refactoring the stream map to return a Pair or process inside.
                 return Duration.ZERO; // Placeholder to fix compilation structure below
            })
            .toList();
            
        // Correct logic:
        List<Duration> validLeadTimes = changes.stream()
            .map(change -> {
                Optional<Deployment> match = findDeploymentFor(change, deployments);
                if (match.isEmpty()) return null;
                return Duration.between(change.createdAt(), match.get().deployedAt());
            })
            .filter(d -> d != null)
            .toList();

        if (validLeadTimes.isEmpty()) {
            return Duration.ZERO;
        }

        long averageSeconds = (long) validLeadTimes.stream()
            .mapToLong(Duration::getSeconds)
            .average()
            .orElse(0);

        return Duration.ofSeconds(averageSeconds);
    }

    private Optional<Deployment> findDeploymentFor(Change change, List<Deployment> deployments) {
        for (MatchStrategy strategy : strategies) {
            Optional<Deployment> match = strategy.findDeployment(change, deployments);
            if (match.isPresent()) {
                return match;
            }
        }
        return Optional.empty();
    }
}