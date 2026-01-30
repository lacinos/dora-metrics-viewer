package com.gemini.dorametricsviewer.domain;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;

import java.util.List;
import java.util.Optional;

public interface MatchStrategy {
    Optional<Deployment> findDeployment(Change change, List<Deployment> deployments);
}
