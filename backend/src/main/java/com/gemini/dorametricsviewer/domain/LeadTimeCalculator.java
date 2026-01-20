package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LeadTimeCalculator {

    public Duration calculate(List<Change> changes, List<Deployment> deployments) {
        Map<String, Deployment> deploymentsBySha = deployments.stream()
            .filter(d -> "SUCCESS".equals(d.status()))
            .collect(Collectors.toMap(
                Deployment::commitSha,
                d -> d,
                (existing, replacement) -> existing
            ));

        List<Duration> leadTimes = changes.stream()
            .filter(change -> deploymentsBySha.containsKey(change.commitSha()))
            .map(change -> {
                Deployment deployment = deploymentsBySha.get(change.commitSha());
                return Duration.between(change.createdAt(), deployment.deployedAt());
            })
            .toList();

        if (leadTimes.isEmpty()) {
            return Duration.ZERO;
        }

        long averageSeconds = (long) leadTimes.stream()
            .mapToLong(Duration::getSeconds)
            .average()
            .orElse(0);

        return Duration.ofSeconds(averageSeconds);
    }
}
