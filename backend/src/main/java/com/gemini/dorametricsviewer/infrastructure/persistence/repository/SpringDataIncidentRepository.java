package com.gemini.dorametricsviewer.infrastructure.persistence.repository;

import com.gemini.dorametricsviewer.infrastructure.persistence.entity.JpaIncidentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpringDataIncidentRepository extends JpaRepository<JpaIncidentEntity, String> {
    List<JpaIncidentEntity> findByRepositoryUrlAndCreatedAtAfter(String repositoryUrl, Instant since);
    List<JpaIncidentEntity> findByRepositoryUrlAndCreatedAtBetween(String repositoryUrl, Instant start, Instant end);
}