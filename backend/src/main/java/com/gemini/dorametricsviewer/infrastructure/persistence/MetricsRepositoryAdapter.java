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
    public void saveDeployment(Deployment deployment) {
        deploymentRepository.save(mapper.toEntity(deployment));
    }

    @Override
    public List<Deployment> findDeployments(String repoUrl, Instant since) {
        return deploymentRepository.findByRepositoryUrlAndDeployedAtAfter(repoUrl, since)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void saveChange(Change change) {
        changeRepository.save(mapper.toEntity(change));
    }

    @Override
    public List<Change> findChanges(String repoUrl, Instant since) {
        return changeRepository.findByRepositoryUrlAndMergedAtAfter(repoUrl, since)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void saveIncident(Incident incident) {
        incidentRepository.save(mapper.toEntity(incident));
    }

    @Override
    public List<Incident> findIncidents(String repoUrl, Instant since) {
        return incidentRepository.findByRepositoryUrlAndCreatedAtAfter(repoUrl, since)
            .stream()
            .map(mapper::toDomain)
            .toList();
    }
}
