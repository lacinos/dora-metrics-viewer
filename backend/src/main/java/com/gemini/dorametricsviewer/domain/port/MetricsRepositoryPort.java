package com.gemini.dorametricsviewer.domain.port;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;

import java.time.Instant;
import java.util.List;

public interface MetricsRepositoryPort {
    void saveDeployments(List<Deployment> deployments);
    List<Deployment> findDeployments(String repoUrl, Instant since);
    List<Deployment> findDeployments(String repoUrl, Instant start, Instant end);

    void saveChanges(List<Change> changes);
    List<Change> findChanges(String repoUrl, Instant since);
    List<Change> findChanges(String repoUrl, Instant start, Instant end);

    void saveIncidents(List<Incident> incidents);
    List<Incident> findIncidents(String repoUrl, Instant since);
    List<Incident> findIncidents(String repoUrl, Instant start, Instant end);
}
