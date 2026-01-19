package com.gemini.dorametricsviewer.domain.port;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;

import java.time.Instant;
import java.util.List;

public interface SourceControlPort {
    List<Deployment> fetchDeployments(String repoUrl, Instant since);
    List<Change> fetchChanges(String repoUrl, Instant since);
    List<Incident> fetchIncidents(String repoUrl, Instant since);
}
