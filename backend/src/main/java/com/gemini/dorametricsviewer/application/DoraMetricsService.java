package com.gemini.dorametricsviewer.application;

import com.gemini.dorametricsviewer.domain.LeadTimeCalculator;
import com.gemini.dorametricsviewer.domain.TimeWindow;
import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.port.MetricsRepositoryPort;
import com.gemini.dorametricsviewer.domain.port.SourceControlPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<Deployment> deployments = sourceControlPort.fetchDeployments(repoUrl, timeWindow.start());
        List<Change> changes = sourceControlPort.fetchChanges(repoUrl, timeWindow.start());

        metricsRepositoryPort.saveDeployments(deployments);
        metricsRepositoryPort.saveChanges(changes);

        // We use the fetched data for calculation.
        var leadTime = leadTimeCalculator.calculate(changes, deployments);

        return new DoraMetricsResult(leadTime);
    }
}