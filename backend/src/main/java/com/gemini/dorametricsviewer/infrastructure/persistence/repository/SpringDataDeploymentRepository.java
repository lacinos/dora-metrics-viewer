package com.gemini.dorametricsviewer.infrastructure.persistence.repository;

import com.gemini.dorametricsviewer.infrastructure.persistence.entity.JpaDeploymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpringDataDeploymentRepository extends JpaRepository<JpaDeploymentEntity, String> {
    List<JpaDeploymentEntity> findByRepositoryUrlAndDeployedAtAfter(String repositoryUrl, Instant since);
}
