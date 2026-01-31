package com.gemini.dorametricsviewer.infrastructure.persistence;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
import com.gemini.dorametricsviewer.domain.port.MetricsRepositoryPort;
import com.gemini.dorametricsviewer.infrastructure.persistence.mapper.MetricsMapper;
import com.gemini.dorametricsviewer.infrastructure.persistence.repository.SpringDataChangeRepository;
import com.gemini.dorametricsviewer.infrastructure.persistence.repository.SpringDataDeploymentRepository;
import com.gemini.dorametricsviewer.infrastructure.persistence.repository.SpringDataIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MetricsRepositoryAdapter implements MetricsRepositoryPort {

    private final SpringDataDeploymentRepository deploymentRepository;
    private final SpringDataChangeRepository changeRepository;
    private final SpringDataIncidentRepository incidentRepository;
    private final MetricsMapper mapper;

    @Override
    public void saveDeployments(List<Deployment> deployments) {
        deploymentRepository.saveAll(deployments.stream()
            .map(mapper::toEntity)
            .toList());
    }

    @Override
    public List<Deployment> findDeployments(String repoUrl, Instant since) {
        return deploymentRepository.findByRepositoryUrlAndDeployedAtAfter(repoUrl, since)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Deployment> findDeployments(String repoUrl, Instant start, Instant end) {
        return deploymentRepository.findByRepositoryUrlAndDeployedAtBetween(repoUrl, start, end)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void saveChanges(List<Change> changes) {
        changeRepository.saveAll(changes.stream()
            .map(mapper::toEntity)
            .toList());
    }

    @Override
    public List<Change> findChanges(String repoUrl, Instant since) {
        return changeRepository.findByRepositoryUrlAndMergedAtAfter(repoUrl, since)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Change> findChanges(String repoUrl, Instant start, Instant end) {
        return changeRepository.findByRepositoryUrlAndMergedAtBetween(repoUrl, start, end)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void saveIncidents(List<Incident> incidents) {
        incidentRepository.saveAll(incidents.stream()
            .map(mapper::toEntity)
            .toList());
    }

    @Override
    public List<Incident> findIncidents(String repoUrl, Instant since) {
        return incidentRepository.findByRepositoryUrlAndCreatedAtAfter(repoUrl, since)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public List<Incident> findIncidents(String repoUrl, Instant start, Instant end) {
        return incidentRepository.findByRepositoryUrlAndCreatedAtBetween(repoUrl, start, end)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
