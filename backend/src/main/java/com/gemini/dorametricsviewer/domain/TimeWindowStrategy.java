package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class TimeWindowStrategy implements MatchStrategy {
    @Override
    public Optional<Deployment> findDeployment(Change change, List<Deployment> deployments) {
        if (change.mergedAt() == null) {
            return Optional.empty();
        }

        // Find the earliest deployment that happened AFTER or ON the merge time
        return deployments.stream()
                .filter(d -> d.deployedAt() != null)
                .filter(d -> !d.deployedAt().isBefore(change.mergedAt()))
                .sorted(Comparator.comparing(Deployment::deployedAt))
                .findFirst();
    }
}
