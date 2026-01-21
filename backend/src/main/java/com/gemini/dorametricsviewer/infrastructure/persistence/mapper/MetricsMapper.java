package com.gemini.dorametricsviewer.infrastructure.persistence.mapper;

import com.gemini.dorametricsviewer.domain.model.Change;
import com.gemini.dorametricsviewer.domain.model.Deployment;
import com.gemini.dorametricsviewer.domain.model.Incident;
import com.gemini.dorametricsviewer.infrastructure.persistence.entity.JpaChangeEntity;
import com.gemini.dorametricsviewer.infrastructure.persistence.entity.JpaDeploymentEntity;
import com.gemini.dorametricsviewer.infrastructure.persistence.entity.JpaIncidentEntity;
import org.springframework.stereotype.Component;

@Component
public class MetricsMapper {

    public JpaDeploymentEntity toEntity(Deployment domain) {
        if (domain == null) return null;
        return new JpaDeploymentEntity(
            domain.id(),
            domain.repositoryUrl(),
            domain.commitSha(),
            domain.createdAt(),
            domain.deployedAt(),
            domain.environment(),
            domain.status()
        );
    }

    public Deployment toDomain(JpaDeploymentEntity entity) {
        if (entity == null) return null;
        return new Deployment(
            entity.getId(),
            entity.getRepositoryUrl(),
            entity.getCommitSha(),
            entity.getCreatedAt(),
            entity.getDeployedAt(),
            entity.getEnvironment(),
            entity.getStatus()
        );
    }

    public JpaChangeEntity toEntity(Change domain) {
        if (domain == null) return null;
        return new JpaChangeEntity(
            domain.id(),
            domain.repositoryUrl(),
            domain.commitSha(),
            domain.createdAt(),
            domain.mergedAt(),
            domain.author()
        );
    }

    public Change toDomain(JpaChangeEntity entity) {
        if (entity == null) return null;
        return new Change(
            entity.getId(),
            entity.getRepositoryUrl(),
            entity.getCommitSha(),
            entity.getCreatedAt(),
            entity.getMergedAt(),
            entity.getAuthor()
        );
    }

    public JpaIncidentEntity toEntity(Incident domain) {
        if (domain == null) return null;
        return new JpaIncidentEntity(
            domain.id(),
            domain.repositoryUrl(),
            domain.createdAt(),
            domain.resolvedAt(),
            domain.severity(),
            domain.description()
        );
    }

    public Incident toDomain(JpaIncidentEntity entity) {
        if (entity == null) return null;
        return new Incident(
            entity.getId(),
            entity.getRepositoryUrl(),
            entity.getCreatedAt(),
            entity.getResolvedAt(),
            entity.getSeverity(),
            entity.getDescription()
        );
    }
}
