package com.gemini.dorametricsviewer.infrastructure.persistence.repository;

import com.gemini.dorametricsviewer.infrastructure.persistence.entity.JpaChangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpringDataChangeRepository extends JpaRepository<JpaChangeEntity, String> {
    List<JpaChangeEntity> findByRepositoryUrlAndMergedAtAfter(String repositoryUrl, Instant since);
    List<JpaChangeEntity> findByRepositoryUrlAndMergedAtBetween(String repositoryUrl, Instant start, Instant end);
}