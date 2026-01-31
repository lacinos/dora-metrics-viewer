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
        System.out.println("DEBUG: Calculating Lead Time for " + changes.size() + " changes and " + deployments.size() + " deployments");
        
        List<Duration> validLeadTimes = changes.stream()
            .map(change -> {
                Optional<Deployment> match = findDeploymentFor(change, deployments);
                if (match.isEmpty()) {
                    System.out.println("DEBUG: No match found for change " + change.id());
                    return null;
                }
                System.out.println("DEBUG: Match found! Change " + change.id() + " -> Deployment " + match.get().id());
                return Duration.between(change.createdAt(), match.get().deployedAt());
            })
            .filter(d -> d != null)
            .toList();

        if (validLeadTimes.isEmpty()) {
            System.out.println("DEBUG: No valid lead times calculated.");
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
                System.out.println("DEBUG: Strategy " + strategy.getClass().getSimpleName() + " matched.");
                return match;
            }
        }
        return Optional.empty();
    }
}
