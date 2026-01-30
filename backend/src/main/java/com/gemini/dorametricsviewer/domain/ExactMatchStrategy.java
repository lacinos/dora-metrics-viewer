package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;

import java.util.List;
import java.util.Optional;

public class ExactMatchStrategy implements MatchStrategy {
    @Override
    public Optional<Deployment> findDeployment(Change change, List<Deployment> deployments) {
        return deployments.stream()
                .filter(d -> d.commitSha().equals(change.commitSha()))
                .findFirst();
    }
}
